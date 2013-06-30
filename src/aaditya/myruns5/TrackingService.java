/**
 * TrackingService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

package aaditya.myruns5;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;


import com.meapsoft.FFT;


// This service will: read and process GPS data.
public class TrackingService extends Service implements LocationListener, SensorEventListener{
	// A buffer list to store all GPS track points
	// It's accessed at different places
	public ArrayList<Location> mLocationList;
	public int mInferenceResult;

	// Sensor manager for accelerometer
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	// Location manager and Notification manager
	private LocationManager mlocationManager;
	private NotificationManager mNotificationManager;

	// Context for "this"
	private Context mContext;

	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mActivityClassificationBroadcast;

	// A blocking queue for buffering motion sensor data
	private static ArrayBlockingQueue<Double> mAccBuffer;

	// The AsyncTask running in a different thread all the time to
	// process the motion sensor data and do classification
	private ActivityClassificationTask mActivityClassificationTask;

	// Based on input type, GPS or automatic, do different things
	private int mInputType;
	// Set up binder for the TrackingService using IBinder
	private final IBinder binder = new MyRunsBinder();	

	// set up the MyRunsBinder 
	public class MyRunsBinder extends Binder
	{
		TrackingService getService()
		{
			return TrackingService.this;
		}
	}	

	@Override
	public IBinder onBind(Intent paramIntent)
	{
		return this.binder;
	}

	@Override
	public void onCreate() {

		// Initialize mContext, mLocationList, mLocationUpdateBroadcast
		mContext = this;
		mLocationList = new ArrayList<Location>(Globals.GPS_LOCATION_CACHE_SIZE);
		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction(Globals.ACTION_LOCATION_UPDATED);

		// Initialize mAccBuffer, mActivityClassificationBroadcast here
		mAccBuffer = new ArrayBlockingQueue<Double>(
				Globals.ACCELEROMETER_BUFFER_CAPACITY);

		mActivityClassificationBroadcast = new Intent();
		mActivityClassificationBroadcast
		.setAction(Globals.ACTION_MOTION_UPDATED);


		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Read inputType, can be GPS or Manual.
		mInputType = intent.getExtras().getInt("input_type");


		// Get LocationManager and set related provider.
		// For indoor debugging, can use network cellular location 
		mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, this);


		// If it's automatic mode, registering motion sensor for activity
		// recognition.
		if (mInputType == Globals.INPUT_TYPE_AUTOMATIC) {
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		    mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
		    mActivityClassificationTask = new ActivityClassificationTask();
			mActivityClassificationTask.execute(new Void[0]);
		}

		// Fire the MapDisplayAcitivty
		Intent localIntent = new Intent(this, MapDisplayActivity.class);

		// Set flags to avoid re-invent activity.
		// http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
		// IMPORTANT!. no re-create activity
		localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// Using pending intent to bring back the MapActivity from notification center.
		PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, localIntent, 0);
		Notification localNotification = new Notification.Builder(this).setContentTitle(this.mContext.getString(R.string.ui_maps_display_notification_title)).setContentText(getResources().getString(R.string.ui_maps_display_notification_content)).setSmallIcon(R.drawable.icon).setContentIntent(localPendingIntent).build();


		// Use NotificationManager to build notification(icon, content, title, flag and pIntent)
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		localNotification.flags = localNotification.flags
				| Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.notify(0, localNotification);


		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Unregistering location manager,
		// foreground notification,
		// and sensor listener.
		// Cancel the mActivityClassificationTask.
		mlocationManager.removeUpdates(this);
		mNotificationManager.cancelAll();

		if (mInputType == Globals.INPUT_TYPE_AUTOMATIC) {
			mSensorManager.unregisterListener(this);
			mActivityClassificationTask.cancel(false);
		}

		super.onDestroy();
		//Log.d(Globals.TAG, "Service Destroyed");
	}

	// Gets called when new GPS location updates
	public void onLocationChanged(Location location) {
		// Check whether location is valid, drop if invalid
		if ((location == null) || (Math.abs(location.getLatitude()) > 90.0D) || (Math.abs(location.getLongitude()) > 180.0D))
			return;

		synchronized (this.mLocationList)
		{
			// Buffer the new location. mLocation is connected by reference by 
			// several other classes. Accessed with "synchronized" lock
			mLocationList.add(location);

			// Send broadcast saying new location is updated
			this.mContext.sendBroadcast(this.mLocationUpdateBroadcast);
			return;
		}

	}
	// You don't need to implement the other three abstract methods of the LocationListener Interface class.:
	// onProviderDisabled, onProviderEnabled, and onStatusChanged. 
	// You can leave them as blank.
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}


	// An AsyncTask running in a separate thread processing the sensor data.
	// It's an infinite loop, waiting on new sensor event, and uses weka
	// classifier to do activity recognition
	private class ActivityClassificationTask extends
	AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			//				Your code  here. Take a look at collector but do not copy 
			//				the code directly. You must understand the collector code
			//				and write it in your own way

			//Instance inst = new DenseInstance(Globals.ACCELEROMETER_BLOCK_CAPACITY+2);
		
			// Create the feature vector for classification
			ArrayList<Double> featVect = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

			double max = Double.MIN_VALUE;

			while (true) {
				try {
					
					if (isCancelled ())
				    {
				        return null;
				    }

					// Dumping buffer
					accBlock[blockSize++] = mAccBuffer.take().doubleValue();

					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						//System.out.println("Buffer size reached");
						blockSize = 0;

						// time = System.currentTimeMillis();
						max = .0;
						for (double val : accBlock) {
							if (max < val) {
								max = val;
							}
						}

						fft.fft(re, im);

						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i]
									* im[i]);
							//inst.setValue(i, mag);
							im[i] = .0; // Clear the field
							featVect.add(mag);
						}

						// Append max after frequency component
						featVect.add(max);
					
						int classifiedValue = (int) WekaClassifier.classify(featVect.toArray());
						System.out.println("Classified value is " + classifiedValue);
						mActivityClassificationBroadcast.putExtra(Globals.KEY_CLASSIFICATION_RESULT,classifiedValue);
						mContext.sendBroadcast(mActivityClassificationBroadcast);
						
						featVect.clear();
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}

		}
	}

	// ----------------------Skeleton--------------------------
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

			// Compute m for 3-axis accelerometer input.
			// m=sqrt(x^2+y^2+z^2)
			double m = Math.sqrt(event.values[0] * event.values[0]
					+ event.values[1] * event.values[1] + event.values[2]
							* event.values[2]);

			// Add m to the mAccBuffer one by one.
			try {
				mAccBuffer.add(new Double(m));
			} catch (IllegalStateException e) {

				// Exception happens when reach the capacity.
				// Doubling the buffer. ListBlockingQueue has no such issue,
				// But generally has worse performance
				ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
						mAccBuffer.size() * 2);

				mAccBuffer.drainTo(newBuf);
				mAccBuffer = newBuf;
				mAccBuffer.add(new Double(m));
			} 
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}

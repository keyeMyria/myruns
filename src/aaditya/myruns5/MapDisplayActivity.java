package aaditya.myruns5;

import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import android.util.Log;


public class MapDisplayActivity extends Activity {

	// Menu ID for deletion
	public static final int MENU_ID_DELETE = 0;

	// Map elements:
	public GoogleMap mMap;
	public Marker start;
	public Marker end;
	public TextView typeStats;
	public TextView avespeedStats;
	public TextView curspeedStats;
	public TextView climbStats;
	public TextView caloriesStats;
	public TextView distanceStats;



	// For bookkeeping if the service bound already
	public boolean mIsBound;
	public boolean mIsDoneDrawing;

	// GPS tracking service
	public TrackingService mSensorService;
	public Intent mServiceIntent;
	public Context mContext;
	public int mTaskType;
	private ArrayList<LatLng> mLatLngList;
	public ArrayList<Location> mLocationList;

	// Exercise entry
	public ExerciseEntryHelper mEntry;

	// Need some special handling if it's the first location update
	public boolean mIsFirstLocUpdate;

	// Use this to draw the start Marker
	public LatLng firstLatLng;


	// A broadcast receiver to receive location update and recenter the map if
	// necessary in another thread.
	private IntentFilter mLocationUpdateFilter;
	private BroadcastReceiver mLocationUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LatLng latlng;
			synchronized (mLocationList) {
				latlng = Utils.fromLocationToLatLng(mLocationList.get(0));
			}

			// Set the first GPS coordinate once get it.
			if (mIsFirstLocUpdate) {
				mIsFirstLocUpdate = false;
				firstLatLng = latlng;
			}

			if (firstLatLng != null) {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
						Globals.DEFAULT_MAP_ZOOM_LEVEL));
			}

			mIsDoneDrawing = false;
			try {
				mEntry.updateStats();
			} catch (Exception e) {

				e.printStackTrace();
			}
			synchronized (mLocationList) {

				// Initialization
				if (mLocationList == null || mLocationList.isEmpty())
					return;

				// Convert the mLocationList to mLatLngList
				for (int i = 0; i < mLocationList.size() - 1; i++) {
					Location loc = mLocationList.get(i);
					mLatLngList.add(Utils.fromLocationToLatLng(loc));
				}

				// Draw Polyline using PolylineOptions

				PolylineOptions polylineOptions = new PolylineOptions();

				// Set Polyline's color
				polylineOptions.color(Color.RED);

				// Set Polyline's width
				polylineOptions.width(5);

				// Add all LatLng points into the list
				polylineOptions.addAll(mLatLngList);

				// Draw the list
				mMap.addPolyline(polylineOptions);

				// Draw marker
				// Initialization
				if (start == null)
					start = mMap
					.addMarker(new MarkerOptions()
					.position(firstLatLng)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

				// Update the latest Marker
				if (end != null)
					end.remove();

				end = mMap
						.addMarker(new MarkerOptions()
						.position(
								Utils.fromLocationToLatLng(mLocationList
										.get(mLocationList.size() - 1)))
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

				// Get real-time stats from the Exercise Entry
				String[] statDecriptions = mEntry.getStatsDescription(mContext);

				// Draw the stats on the map
				if (statDecriptions.length != 0) {
					typeStats.setText(statDecriptions[0]);
					avespeedStats.setText(statDecriptions[1]);
					curspeedStats.setText(statDecriptions[2]);
					climbStats.setText(statDecriptions[3]);
					caloriesStats.setText(statDecriptions[4]);
					distanceStats.setText(statDecriptions[5]);

				}
				// Clear the mLatLngList
				mLatLngList.removeAll(mLatLngList);
			}
			mIsDoneDrawing = true;

		}
	};


	// Set up the mMotionUpdateIntentFilter broadcast receiver to update
	// activity inference using onReceive(). Gets the classification results
	// from the TrackingService and updates mEntry.updateByInference


	private IntentFilter mMotionUpdateIntentFilter;
	private BroadcastReceiver mMotionUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//
			int val = intent.getIntExtra(Globals.KEY_CLASSIFICATION_RESULT, -1);
			mEntry.updateByInference(val);
		}
	};

	// Create new ServiceConnection
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {

			// Initialize mSensorService from TrackingService
			mSensorService = ((TrackingService.MyRunsBinder)service).getService();

			// Get mLocationList from mSensorService
			mLocationList = mSensorService.mLocationList;

			// set Location list for mEntry.
			mEntry.setLocationList(mLocationList);

			// Start logging
			try
			{
				mEntry.startLogging();
				return;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}


		}

		public void onServiceDisconnected(ComponentName name) {
			// Stop the service. This ONLY gets called when crashed.
			Log.d("MyRuns", "Connection disconnected");
			stopService(mServiceIntent);
			mSensorService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Inflate the map_display
		setContentView(R.layout.map_display);

		// Initialize the mLatLngList.
		mLatLngList = new ArrayList(100000);

		// Find all 6 TextView widgets using their resource id.
		typeStats = ((TextView)findViewById(R.id.type_stats));
		avespeedStats = ((TextView)findViewById(R.id.avespeed_stats));
		curspeedStats = ((TextView)findViewById(R.id.curspeed_stats));
		climbStats = ((TextView)findViewById(R.id.climb_stats));
		caloriesStats = ((TextView)findViewById(R.id.calories_stats));
		distanceStats = ((TextView)findViewById(R.id.distance_stats));

		// Set context.
		mContext = this;


		// Initialize mEntry.
		mEntry = new ExerciseEntryHelper();

		// Initialize the Bound flag.
		mIsBound = false;


		// Get extras from intent and set the mTaskType, InputType, Row Id and ActivityType
		Bundle extras = getIntent().getExtras();
		mTaskType = extras.getInt(Globals.KEY_TASK_TYPE,
				Globals.TASK_TYPE_ERROR);
		mEntry.setInputType(extras.getInt(Globals.KEY_INPUT_TYPE,
				Globals.INPUT_TYPE_ERROR));
		mEntry.setActivityType(extras.getInt(Globals.KEY_ACTIVITY_TYPE,
				Globals.ACTIVITY_TYPE_ERROR));
		mEntry.setID(extras.getLong(Globals.KEY_ROWID, -1));


		// Get google map from the MapFragment
		mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

		// Different initialization based on different task type and input mode
		// Combinations can be (new or history) and (gps or automatic). Manual
		// mode is handled in ManualInputActivity
		// The difference between new and history is:
		// "new" task type needs a service to read sensor data. "gps"
		// mode only pulls the GPS locations, and "automatic" mode also pull motion sensor data
		// for Weka classifier  While the "history" task type reads from database, and display the
		// map route only, does not need sensor service.
		switch (mTaskType) {

		case Globals.TASK_TYPE_NEW: //1
			Bundle extras2 = new Bundle();
			extras2.putInt("TASK_TYPE", mTaskType);
			extras2.putInt("input_type", mEntry.getInputType());


			// Register the GPS location sensor to receive location update
			mLocationUpdateFilter = new IntentFilter();
			mLocationUpdateFilter.addAction(Globals.ACTION_LOCATION_UPDATED);
			mIsFirstLocUpdate = true;

			// If in the automatic mode, also register the
			// motion sensor intent filter
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				mMotionUpdateIntentFilter = new IntentFilter();
				mMotionUpdateIntentFilter
				.addAction(Globals.ACTION_MOTION_UPDATED);
			}

			mServiceIntent = new Intent(this, TrackingService.class);
			mServiceIntent.putExtras(extras2);

			// Start and bind the tracking service
			startService(mServiceIntent);
			doBindService();
			break;

		case Globals.TASK_TYPE_HISTORY: //2
			// No longer need "Save" and "Cancel" button in history mode
			((Button)findViewById(R.id.btnSave)).setVisibility(8);
			((Button)findViewById(R.id.btnCancel)).setVisibility(8);
			try
			{	// Read track from database
				if (mEntry == null)
					Log.e("Entry", "Entry is null");

				mEntry.readFromDB(mContext);
				mLocationList = mEntry.getLocationList();


				// Convert the mLocationList to mLatLngList
				// so that you can draw polylines using LatLng objects

				if (mLocationList.size() > 0) {
					for (int i = 0; i < mLocationList.size(); i++) {
						Location localLocation = (Location)this.mLocationList.get(i);
						mLatLngList.add(Utils.fromLocationToLatLng(localLocation));
					}

					// Draw marker for the start point
					mMap.addMarker(new MarkerOptions().position((LatLng)mLatLngList.get(0)).icon(BitmapDescriptorFactory.defaultMarker(120.0F)));

					// Draw marker for the end point
					mMap.addMarker(new MarkerOptions().position((LatLng)mLatLngList.get(mLatLngList.size() - 1)).icon(BitmapDescriptorFactory.defaultMarker(0.0F)));

					// Draw the GPS traces, set the width, color and use addAll to
					// write a Polyline that goes through all the LatLng points
					PolylineOptions localPolylineOptions = new PolylineOptions();
					localPolylineOptions.color(-65536);
					localPolylineOptions.width(5.0F);
					localPolylineOptions.addAll(mLatLngList);
					mMap.addPolyline(localPolylineOptions);

					// Move map center to the 1st point in the route track.
					if (!mLatLngList.isEmpty())
					{
						LatLng localLatLng = (LatLng)mLatLngList.get(0);
						if ( ifMapNeedRecenter(localLatLng) ) {
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localLatLng, 17.0F));
					
						}
					}

					// Clear the mLatLngList
					mLatLngList.removeAll(this.mLatLngList);

					// Get previous stats from the ExerciseEntry
					String[] stats = mEntry.getStatsDescription(mContext);

					// Draw the stats on the map
					if (stats.length > 0) {
						switch (mEntry.getActivityType()) {
						case Globals.ACTIVITY_TYPE_WALKING:
							typeStats.setText("Type: Walking");
							break;
						case Globals.ACTIVITY_TYPE_RUNNING:
							typeStats.setText("Type: Running");
							break;
						case Globals.ACTIVITY_TYPE_STANDING:
							typeStats.setText("Type: Standing");
							break;
						default:
							break;
						}
						avespeedStats.setText(stats[1]);
						curspeedStats.setText(stats[2]);
						climbStats.setText(stats[3]);
						caloriesStats.setText(stats[4]);
						distanceStats.setText(stats[5]);
					} else Log.e("Entry", "Stats are not of correct length");
				}
				break;
			}
			catch (Exception localException)
			{
				localException.printStackTrace();
				return;

			}

		default:
			finish(); // Should never happen.
			return;
		}
	}

	public void onDestroy() {
		// Stop the service and the notification.
		// Need to check whether the mSensorService is null or not.
		if(mSensorService!=null)
		{
			mSensorService.stopForeground(true);
			doUnbindService();
			stopService(mServiceIntent);
		}
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		finish();
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		// Register the receiver for receiving the location update broadcast
		// from the service. Logic is the same as in onCreate()

		// If "new" task, need to read sensor data.
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			// Register gps location update receiver
			registerReceiver(mLocationUpdateReceiver, mLocationUpdateFilter);			 
			
			// If it's "automatic" mode, also need motion sensor for
			// classification
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				registerReceiver(mMotionUpdateReceiver,
						mMotionUpdateIntentFilter);
			}

		}



		super.onResume();
	}

	@Override
	protected void onPause() {
		// Unregister the receiver when the activity is about to go inactive
		// Reverse to what happened in onResume()
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			unregisterReceiver(mLocationUpdateReceiver);
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				unregisterReceiver(mMotionUpdateReceiver);
			}
		}

		//doUnbindService();
		//Log.d(Globals.TAG, "Activity paused");
		super.onPause();
	}

	public void onSaveClicked(View v) {

		// Multiple click will give duplicate entries, disable the button
		// immediately after 1st click.
		v.setEnabled(false);

		// Insert the ExerciseEntry to database, return value should be the entry id.
		long id=mEntry.insertToDB(mContext);;

		if (id > 0) {
			Toast.makeText(getApplicationContext(), "Entry #" + id + " saved.",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Entry not saved.",
					Toast.LENGTH_SHORT).show();
		}

		// Stop the service in foreground, unbind and cancel the service(also the notification).
		mSensorService.stopForeground(true);
		doUnbindService();
		stopService(mServiceIntent);
		((NotificationManager)getSystemService("notification")).cancelAll();
		finish();
	}

	public void onCancelClicked(View v) {
		// Similar to what happened in onSaveClicked() but without the insertToDB
		// operation.

		v.setEnabled(false);

		// Stop the service and the notification.
		mSensorService.stopForeground(true);
		doUnbindService();
		stopService(mServiceIntent);
		((NotificationManager)getSystemService("notification")).cancelAll();

		finish();
	}

	@Override
	public void onBackPressed() {
		// When back is pressed, similar to onCancelClicked, stop service and the notification.
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			mSensorService.stopForeground(true);
			doUnbindService();
			stopService(this.mServiceIntent);
			((NotificationManager)getSystemService("notification")).cancelAll();
		}

		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// If task type is displaying history, also give a menu button
		// To delete the entry
		MenuItem menuitem;
		if (mTaskType == Globals.TASK_TYPE_HISTORY) {
			menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE,
					"Delete");
			menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_DELETE:
			// Delete entry in database
			mEntry.deleteEntryInDB(mContext);
			finish();
			return true;
		default:
			finish();
			return false;
		}
	}

	private void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).

		if (!mIsBound) {
			bindService(this.mServiceIntent, this.connection, 1);
			mIsBound = true;
		}

	}

	private void doUnbindService() {
		if (mIsBound) {
			// Double unbind behaves like double free. So check first.
			// Detach our existing connection.
			unbindService(this.connection);
			mIsBound = false;
		}
	}

	// Make sure current location falls into center area of the screen
	// Otherwise re center the map
	private boolean ifMapNeedRecenter(LatLng latlng) {
		// Gets a projection of the viewing frustum for 
		// converting between screen coordinates and 
		// geo-latitude/longitude coordinates.

		//System.out.println("If map need Recenter called for coordinates " + latlng.longitude + " and " + latlng.latitude);

		VisibleRegion vr = mMap.getProjection().getVisibleRegion();

		double left = vr.latLngBounds.southwest.longitude;
		double top = vr.latLngBounds.northeast.latitude;
		double right = vr.latLngBounds.northeast.longitude;
		double bottom = vr.latLngBounds.southwest.latitude;


		int rectWidth = (int) Math.abs(right - left);
		int rectHeight = (int) Math.abs(top - bottom);

		int rectCenterX = (int) mMap.getCameraPosition().target.longitude;
		int rectCenterY = (int) mMap.getCameraPosition().target.latitude;

		// Constructs the rectangle
		Rect validScreenRect = new Rect(rectCenterX - rectWidth / 2,
				rectCenterY - rectHeight / 2, rectCenterX + rectWidth / 2,
				rectCenterY + rectHeight / 2);
		//System.out.println("Returning from ifMapNeedRecenter");
		return !validScreenRect.contains((int) latlng.longitude,
				(int) latlng.latitude);
	}
}

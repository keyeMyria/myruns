package aaditya.myruns5;

import java.util.ArrayList;
import java.util.Arrays;

import android.location.Location;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

//ExerciseEntryHelper operates on the bare-bone ExerciseEntry with more flexibility,  
//adds database operations and other related operations 
public class ExerciseEntryHelper
{
	// The ExerciseEntry
	private ExerciseEntry mData;

	// Extra useful status properties
	private double mCurSpeed; // Current speed
	private boolean mIsLoggingStarted; // is logging started
	private ArrayList<Location> mLocationList; // Location list
	private int mNLocations; // Number of location points
	private GregorianCalendar mTimeStarted; // Started time
	private Location[] mTrack; // Location array

	private int mCurrentInferedActivityType; // Current inferred activity type.
	// for automatic mode
	private int[] mInferenceCount; // Count of the inference results for voting
	// decision
	// Formatter
	public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

	public ExerciseEntryHelper() {
		mData = new ExerciseEntry();
		mInferenceCount = new int[Globals.ACTIVITY_TYPES.length];
	}

	//************** Database operations ************** //

	// Write the current exercise entry into database.

	public long insertToDB(Context paramContext)
	{

		// Check task type. If manual, no track data. else, get track
		// from location list
		if (mData.getInputType() != Globals.INPUT_TYPE_MANUAL) {
			assert (mLocationList != null);

			synchronized (mLocationList) {
				if (mLocationList.size() < 2) {
					return -1;
				}
				mTrack = new Location[mLocationList.size()];
				mTrack = mLocationList.toArray(mTrack);
			}
		}

		//create a ContentValues object to store exercise entry.
		ContentValues values = new ContentValues();

		//put all the data saved in ExerciseEntry into the ContentValues object.
		values.put("input_type", Integer.valueOf(this.mData.getInputType()));
		values.put("activity_type", Integer.valueOf(this.mData.getActivityType()));
		values.put("date_time", Long.valueOf(this.mData.getDateTime().getTime() / 1000L));
		values.put("duration", Integer.valueOf(this.mData.getDuration()));
		values.put("distance", Double.valueOf(this.mData.getDistance()));
		values.put("calories", Integer.valueOf(this.mData.getCalorie()));
		values.put("heartrate", Integer.valueOf(this.mData.getHeartrate()));
		values.put("avg_speed", Double.valueOf(this.mData.getAvgSpeed()));
		values.put("climb", Double.valueOf(this.mData.getClimb()));
		values.put("comment", this.mData.getComment());

		// If the input type is GPS/Automatic, then add the mTrack to the
		// database.
		if (mTrack != null)
			values.put(HistoryTable.KEY_GPS_DATA,
					Utils.fromLocationArrayToByteArray(mTrack));

		//get the content resolver, insert the ContentValues into HistoryProvider.
		Uri localUri = paramContext.getContentResolver().insert(HistoryProvider.CONTENT_URI, values);

		//set current ExerciseEntry's id and return the current ExerciseEntry's id.
		mData.setId(Long.valueOf(localUri.getLastPathSegment()));
		return Long.valueOf(localUri.getLastPathSegment()).longValue();
	}

	// Read an exercise entry specified by the id field from database
	public void readFromDB(Context context) throws Exception {

		long id = mData.getId().longValue();

		if (id <= 0) {
			throw new Exception();
		}

		// Cursor has all column values of the entry specified by id
		Cursor c = context.getContentResolver().query(
				Uri.parse(HistoryProvider.CONTENT_URI + "/"
						+ String.valueOf(id)), null, null, null, null);
		// Initialize the cursor
		c.moveToFirst();

		// Fill entry member variables from cursor using setters.

		setID(id);
		setInputType(c.getInt(c.getColumnIndex(Globals.KEY_INPUT_TYPE)));
		setActivityType(c.getInt(c.getColumnIndex(Globals.KEY_ACTIVITY_TYPE)));

		setDateTime(c.getLong(c.getColumnIndex(Globals.KEY_DATE_TIME)) * 1000);
		setDuration(c.getInt(c.getColumnIndex(Globals.KEY_DURATION)));
		setDistance(c.getInt(c.getColumnIndex(Globals.KEY_DISTANCE)));
		setClimb(c.getInt(c.getColumnIndex(Globals.KEY_CLIMB)));
		setInputType(c.getInt(c.getColumnIndex(Globals.KEY_INPUT_TYPE)));
		setCalories(c.getInt(c.getColumnIndex(Globals.KEY_CALORIES)));
		setAvgSpeed(c.getDouble(c.getColumnIndex(Globals.KEY_AVG_SPEED)));
		setComment(c.getString(c.getColumnIndex(Globals.KEY_COMMENT)));
		setHeartrate(c.getInt(c.getColumnIndex(Globals.KEY_HEARTRATE)));

		// Read GPS traces into byte Array.
		byte[] byteTrack = c.getBlob(c.getColumnIndex(Globals.KEY_GPS_DATA));

		Location[] locarray = Utils.fromByteArrayToLocationArray(byteTrack);

		// Set the location list.
		ArrayList<Location> loclist = new ArrayList<Location>(
				Arrays.asList(locarray));
		setLocationList(loclist);

		// Close the cursor.
		c.close();

	}



	//Delete a entry specified by the argument id.
	// Static method, more general.
	public static void deleteEntryInDB(Context paramContext, long paramLong)
	{
		paramContext.getContentResolver().delete(Uri.parse(HistoryProvider.CONTENT_URI + "/" + String.valueOf(paramLong)), null, null);
	}

	// Overloading class function to delete current entry.
	public void deleteEntryInDB(Context paramContext)
	{
		deleteEntryInDB(paramContext, getID());
	}

	// ************** Statistics operations ************** //

	// Return a description about the statistics of the exercise for drawing
	// on the overlay. Gets called in overlay onDraw event.
	// We give you the code for this operation

	// The stats are shown when the map is visible and you have started an
	// new exercise
	// and when you look at saved exercise using history. The stats are shown in
	// the top
	// left hand corner of the map. Check out the APK to see what the stats are
	// 	
	public String[] getStatsDescription(Context context) {

		String[] stats = new String[6];

		String[] activity_types = context.getResources().getStringArray(
				R.array.activity_type_items);
		String speed_measure_units = context.getString(R.string.mile_per_hr);
		String climb_measure_units = context.getString(R.string.feet);
		String distance_measure_units = context.getString(R.string.miles);

		int activity_type_to_display;


		if (getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
			activity_type_to_display = getCurrentInferedActivityType();
		} else {
			activity_type_to_display = getActivityType();
		}


		// Multi-line string
		stats[0] = "Type: " + activity_types[activity_type_to_display];
		stats[1] = "Avg speed: " + decimalFormat.format(getAvgSpeed()) + " "
				+ speed_measure_units;
		stats[2] = "Cur speed: " + decimalFormat.format(getCurSpeed()) + " "
				+ speed_measure_units;
		stats[3] = "Climb: " + decimalFormat.format(getClimb()) + " "
				+ climb_measure_units;
		stats[4] = "Calorie: " + decimalFormat.format(getCalories());
		stats[5] = "Distance: "
				+ decimalFormat.format(getDistance() / Globals.KILO
						/ Globals.KM2MILE_RATIO) + " " + distance_measure_units;

		return stats;
	}

	// Start and initialize the variables for computing the statistics
	// You have to code this operation.

	public void startLogging() throws Exception {

		// You need to initial the statistics data that will be displayed as on
		// the upper left part of the map including (please take a look at the
		// apk)

		if (mLocationList == null
				|| mData.getInputType() == Globals.INPUT_TYPE_MANUAL) {
			throw new Exception();
		}

		mData.setDistance(0.0D);
		mData.setDuration(0);
		mData.setCalorie(0);
		mData.setClimb(0.0D);
		mData.setAvgSpeed(0.0D);
		mNLocations = 0;
		mTimeStarted = new GregorianCalendar();
		mIsLoggingStarted = true;
		mCurrentInferedActivityType = Globals.ACTIVITY_TYPE_STANDING; // Initially
		// still

	}

	// ----------------------Skeleton--------------------------
	// Update the stats based on the newly acquired data in mLocationList
	// You have to code this operation.

	public void updateStats() throws Exception {
		if (mLocationList == null
				|| mData.getInputType() == Globals.INPUT_TYPE_MANUAL
				|| !mIsLoggingStarted) {
			throw new Exception();
		}
		// ----------------------Skeleton--------------------------
		// Dumping mLocationList to Location[] track
		synchronized (mLocationList) {

			if (mLocationList.size() == mNLocations || mLocationList.size() < 2) {
				return;
			}
			// ----------------------Skeleton--------------------------
			// Check where to start, edge case is when just started.
			int iStart = (mNLocations == 0) ? 1 : mNLocations;
			Location prevLoc, currLoc;
			for (int i = iStart; i < mLocationList.size(); i++) {
				prevLoc = mLocationList.get(i - 1);
				currLoc = mLocationList.get(i);

				mData.setDistance(mData.getDistance()
						+ prevLoc.distanceTo(currLoc));

				if (prevLoc.hasAltitude() && currLoc.hasAltitude()
						&& prevLoc.getAltitude() < currLoc.getAltitude()) {
					mData.setClimb(mData.getClimb() + currLoc.getAltitude()
							- prevLoc.getAltitude());
				}
			}
			mNLocations = mLocationList.size();

			mCurSpeed = mLocationList.get(mNLocations - 1).hasSpeed() ? mLocationList
					.get(mNLocations - 1).getSpeed() : .0;
		}
		// ----------------------Skeleton--------------------------
		// Set duration, Calorie and AvgSpeed
		mData.setDuration((int) ((System.currentTimeMillis() - mTimeStarted
				.getTimeInMillis()) / 1000));

		mData.setCalorie((int) mData.getDistance() / 15);

		mData.setAvgSpeed(mData.getDuration() > 0 ? mData.getDistance()
				/ mData.getDuration() : .0);


	}

	// This function updates the inferred activity type from the new Weka
	// classifier result
	// There are two things: mCurrentInferedActivityType is the real time
	// result, and
	// mData.activityType is the overall activity type by voting on all real
	// time results from start until
	// now. ( Say we have three results, two are running and one is walking, so
	// by voting
	// the final inferred type is running.)
	public void updateByInference(int result) {

		// Log.d(Globals.TAG, "passed val: " + result);

		// mInferenceCount is like a histogram, bookkeeping the results history

		// Notice the mapping between classifier result index and activity type
		// index in the database:
		// They are defined at Globals.INFERENCE_MAPPTING_ID_[xxxxxx]
		// mCurrentInferedActivityType is what we want to display on the stats.
		//
		switch (result) {
		case Globals.INFERENCE_ID_STANDING:
			mInferenceCount[Globals.ACTIVITY_TYPE_STANDING]++;
			mCurrentInferedActivityType = Globals.ACTIVITY_TYPE_STANDING;
			break;
		case Globals.INFERENCE_ID_WALKING:
			mInferenceCount[Globals.ACTIVITY_TYPE_WALKING]++;
			mCurrentInferedActivityType = Globals.ACTIVITY_TYPE_WALKING;
			break;
		case Globals.INFERENCE_ID_RUNNING:
			mInferenceCount[Globals.ACTIVITY_TYPE_RUNNING]++;
			mCurrentInferedActivityType = Globals.ACTIVITY_TYPE_RUNNING;
			break;
		default:
			break;
		}
		
		int curr_max_val = 0;
		int curr_max_index = Globals.ACTIVITY_TYPE_STANDING;
		
		// Set the overall activity type from voting using setActivityType()
		for (int i = 0; i < mInferenceCount.length; i ++) {
			if ( mInferenceCount[i] > curr_max_val ) {
				curr_max_val = mInferenceCount[i];
				curr_max_index = i;
			}
		}
		
		setActivityType(curr_max_index);
	}


	// *******************************************************//
	// Standard setters and getters. May have some conversions.
	public void setLocationList(ArrayList<Location> list) {
		mLocationList = list;
	}

	public ArrayList<Location> getLocationList() {
		return mLocationList;
	}

	public int getActivityType() {
		return mData.getActivityType();
	}

	public int getCurrentInferedActivityType() {
		return mCurrentInferedActivityType;
	}

	public double getDistance() {
		return mData.getDistance();
	}

	public int getCalories() {
		return mData.getCalorie();
	}

	public double getClimb() {
		return mData.getClimb();
	}

	public double getCurSpeed() {
		double rv = mCurSpeed / Globals.KILO * Globals.SECONDS_PER_HOUR
				/ Globals.KM2MILE_RATIO;
		return rv;
	}

	// in mile/hour
	public double getAvgSpeed() {
		return mData.getAvgSpeed() / Globals.KILO * Globals.SECONDS_PER_HOUR
				/ Globals.KM2MILE_RATIO;
	}

	public void setDate(int year, int month, int day) {

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(mData.getDateTime());

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		cal = new GregorianCalendar(year, month, day, hour, minute, second);

		mData.setDateTime(cal.getTime());
	}

	public void setTime(int hour, int minute, int second) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(mData.getDateTime());

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		cal = new GregorianCalendar(year, month, day, hour, minute, second);

		mData.setDateTime(cal.getTime());
	}

	public void setDateTime(long timeInMS) {
		mData.setDateTime(new Date(timeInMS));
	}

	public void setDuration(int durationInSeconds) {
		mData.setDuration(durationInSeconds);
	}

	public void setDistance(int distanceInMeters) {
		mData.setDistance(distanceInMeters);
	}

	public void setCalories(int calories) {
		mData.setCalorie(calories);
	}

	public void setHeartrate(int heartrate) {
		mData.setHeartrate(heartrate);
	}

	public void setComment(String comment) {
		mData.setComment(comment);
	}

	public void setActivityType(int activityTypeCode) {
		mData.setActivityType(activityTypeCode);
	}

	public void setInputType(int inputTypeCode) {
		mData.setInputType(inputTypeCode);
	}

	public void setClimb(int climb) {
		mData.setClimb(climb);
	}

	public void setAvgSpeed(double speed) {
		mData.setAvgSpeed(speed);
	}

	public void setID(long id) {
		mData.setId(Long.valueOf(id));
	}

	public long getID() {
		return mData.getId().longValue();
	}

	public int getInputType() {
		return mData.getInputType();
	}
}

/**
 * ActivityTabEntries.java
 * 
 * Created by Xiaochao Yang on Dec 9, 2011 10:53:06 PM
 * 
 */

package aaditya.myruns5;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

// Display the history of exercises in a list view. 
// For each item in the list, based on the type, can be GPS||Automatic or Manual,
// different activities will be launched to view the details: MapDisplayActivity or DisplayEntryActivity

public class HistoryTabFragment extends ListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {
	
	

	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 0;

	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	
	public Context mContext; // context pointed to parent activity
	public ActivityEntriesCursorAdapter mAdapter; // customized adapter for displaying entreis
	public Cursor mActivityEntryCursor; // cursor returned from database query.
	
	// Table column index
	public int mRowIdIndex;
	public int mActivityIndex;
	public int mTimeIndex;
	public int mDurationIndex;
	public int mDistanceIndex;
	public int mCaloriesIndex;
	public int mHeartrateIndex;
	public int mCommentIndex;
	public int mInputTypeIndex;
	
	
	// Different format to display the information
	public static final String DATE_FORMAT = "H:mm:ss MMM d yyyy";
	public static final String DISTANCE_FORMAT = "#.##";
	public static final String MINUTES_FORMAT = "%d mins";
	public static final String SECONDS_FORMAT = "%d secs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		
		mActivityEntryCursor = getActivity().getContentResolver().query(
				HistoryProvider.CONTENT_URI,
				null,
			    null, null, null);
		

		// Read the column index of the database table
		mRowIdIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_ROWID);
		mActivityIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_ACTIVITY_TYPE);
		mTimeIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_DATE_TIME);
		mDurationIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_DURATION);
		mDistanceIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_DISTANCE);
		mCaloriesIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_CALORIES);
		mHeartrateIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_HEARTRATE);
		mCommentIndex = mActivityEntryCursor
				.getColumnIndex(Globals.KEY_COMMENT);
		mInputTypeIndex = mActivityEntryCursor
				.getColumnIndexOrThrow(Globals.KEY_INPUT_TYPE);

		mCallbacks = this;

		// Initialize the Loader with id "0" and callbacks "mCallbacks".
		
		// If the loader doesn't already exist, one is created. Otherwise,
		// the already created Loader is reused. In either case, the
		// LoaderManager will manage the Loader across the Fragment
		// lifecycle, will receive any new loads once they have completed,
		// and will report this new data back to the "mCallbacks" object.
		
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);

	    
		// Open data base for operations.
	    mAdapter = new ActivityEntriesCursorAdapter(getActivity(),
				mActivityEntryCursor);
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.history, container, false);
	}



	// Click event 
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l,v,position,id);
		Intent intent = new Intent(); // The intent to launch the activity after
										// click.
		Bundle extras = new Bundle(); // The extra information needed pass
										// through to next activity.
		mActivityEntryCursor = mAdapter.getCursor();
	
		// Task type is display history, versus create new as in StartTabFragment.java
	    extras.putInt(Globals.KEY_TASK_TYPE,
						Globals.TASK_TYPE_HISTORY);
		// Write row id into extras.
		extras.putLong(Globals.KEY_ROWID,
				id);

		
		// Read the input type: Manual, GPS, or automatic
        // Write the input type
		int inputType=-1;
	
		// Clicked position is the row number in the database
		mActivityEntryCursor.moveToPosition(position);

		inputType = mActivityEntryCursor.getInt(mInputTypeIndex);

		// Based on different input type, launching different activities
		switch (inputType) {
		
		case Globals.INPUT_TYPE_GPS:
		case Globals.INPUT_TYPE_AUTOMATIC:
			// GPS and Automatic require MapDisplayAcvitity
			extras.putInt(Globals.KEY_ACTIVITY_TYPE, mActivityEntryCursor
							.getInt(mActivityIndex));
			intent.setClass(mContext, MapDisplayActivity.class);
			break;
			
		case Globals.INPUT_TYPE_MANUAL: // Manual mode
			
			// Passing information for display in the DisaplayEntryActivity.
						extras.putString(Globals.KEY_ACTIVITY_TYPE,
								parseActivityType(mActivityEntryCursor
										.getInt(mActivityIndex)));
						extras.putString(Globals.KEY_DATE_TIME,
								parseTime(mActivityEntryCursor.getLong(mTimeIndex)));
						extras.putString(Globals.KEY_DURATION,
								parseDuration(mActivityEntryCursor.getInt(mDurationIndex)));
						extras.putString(Globals.KEY_DISTANCE,
								parseDistance(mActivityEntryCursor
										.getDouble(mDistanceIndex)));
						extras.putString(Globals.KEY_CALORIES,
								String.valueOf((mActivityEntryCursor.getInt(mCaloriesIndex))));
						extras.putString(Globals.KEY_HEARTRATE,
								String.valueOf((mActivityEntryCursor.getInt(mHeartrateIndex))));
						extras.putString(Globals.KEY_COMMENT,
								mActivityEntryCursor
										.getString(mCommentIndex));

			// Manual mode requires DisplayEntryActivity
			intent.setClass(mContext, DisplayEntryActivity.class);
			break;
		default:
			return;
		}
				
		intent.putExtras(extras);
		startActivity(intent);
	}

	// Subclass a cursor adapter for our purpose.
	// Display interpreted database row values in customized list view.
	private class ActivityEntriesCursorAdapter extends CursorAdapter {

		private LayoutInflater mInflater;

		public ActivityEntriesCursorAdapter(Context context, Cursor c) {
			super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
			mInflater = LayoutInflater.from(context);
		}
		
		//Override the BindView function to set our data which means, 
		//take the data from the cursor and put it into views.
		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			// Setting up view's text1 is main title, text2 is sub-title.
			TextView titleView = (TextView) view
					.findViewById(android.R.id.text1);
			TextView summaryView = (TextView) view
					.findViewById(android.R.id.text2);

			// use cursor getInt/Double to read value, and convert with
			// parser utility functions.
			System.out.println("Activity index" + mActivityIndex);
     		String activityTypeString = parseActivityType(cursor.getInt(mActivityIndex));
			String dateString = parseTime(cursor.getLong(mTimeIndex));
			String distanceString = parseDistance(cursor
					.getDouble(mDistanceIndex));
			String durationString = parseDuration(cursor.getInt(mDurationIndex));

			// Set text on the view. +
			titleView.setText(activityTypeString+ ", " + dateString);
			summaryView.setText(distanceString + ", " + durationString);
		}

		
		// When the view will be created for first time,
        // we need to tell the adapters, how each item will look
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(android.R.layout.two_line_list_item, null);
		}
	}
	
	// *******************************//
	// Parser utilities to read value from database and interpret into human-readable string 
	
	// From activity type 0, 1, 2 ... to string "Running", "Walking", etc.
	private String parseActivityType(int code) {
		String activityTypes[] = getResources().getStringArray(
				R.array.activity_type_items);

		//System.out.println("Code " + code);
		return activityTypes[code];
	}

	// From 1970 epoch time in seconds to something like "10/24/2012" 
	private String parseTime(long timeInSec) {

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(timeInSec * 1000);
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat(DATE_FORMAT);

		return dateFormat.format(calendar.getTime());
	}

	// Round the double type distance in meters shorter. 
	// (Can also do some conversion mile-kilometers stuff)
	private String parseDistance(double distInMeters) {

		double distInMiles = distInMeters / 1000.0 / Globals.KM2MILE_RATIO;
		String unit = getActivity().getString(R.string.miles);

		DecimalFormat decimalFormat = new DecimalFormat(DISTANCE_FORMAT);
		return decimalFormat.format(distInMiles) + " " + unit;
	}

	// Convert duration in seconds to minutes.
	private String parseDuration(int durationInSeconds) {
		return durationInSeconds > 60 ? String.format(MINUTES_FORMAT,
				durationInSeconds / 60) : String.format(SECONDS_FORMAT,
				durationInSeconds);

	}
	
	
	//skeleton
	//The LoaderManager helps the HistoryTabFragment to 
	//(1) load data on a separate thread, and
	//(2) monitor the underlying data source for updates, re-querying when changes are detected.
	//(3) refresh the cursor and update the cursor adapter.
	
	// To use the LoaderManager, you need to implements three LoaderManager callbacks in the HistoryTabFragment. 
	
	
	// Create a new CursorLoader with the following query parameters.
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		return new CursorLoader(getActivity(), HistoryProvider.CONTENT_URI,
				null, null, null, null);
	}

	//skeleton
	//When the load finished, swap the cursor for the cursor adapter.
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
//		switch (loader.getId()) {
//		case LOADER_ID:
//			// The asynchronous load is complete and the data
//			// is now available for use. Only now can we associate
//			// the queried Cursor with the Cursor Adapter.
			mAdapter.swapCursor(cursor);
//			break;
//		}
		// The listview now displays the queried data.
	}
	
	//skeleton
	// For whatever reason, the Loader's data is now unavailable.
	// Remove any references to the old data by replacing it with
	// a null Cursor.
	public void onLoaderReset(Loader<Cursor> loader) {
		
		mAdapter.swapCursor(null);
	}

}

package aaditya.myruns5;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ManualInputActivity extends ListActivity
{
	public static final int LIST_ITEM_ID_CALORIES = 4;
	public static final int LIST_ITEM_ID_COMMENT = 6;
	public static final int LIST_ITEM_ID_DATE = 0;
	public static final int LIST_ITEM_ID_DISTANCE = 3;
	public static final int LIST_ITEM_ID_DURATION = 2;
	public static final int LIST_ITEM_ID_HEARTRATE = 5;
	public static final int LIST_ITEM_ID_TIME = 1;
	public ExerciseEntryHelper mEntry;


	protected void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);

		// Setting the UI layout
		setContentView(R.layout.manualinput);

		// Initialize the ExerciseEntryHelper()
		this.mEntry = new ExerciseEntryHelper();

		// Get the extra information passed from the launching activity
		Bundle localBundle = getIntent().getExtras();

		//set InputType and ActivityType from the extras.
		this.mEntry.setInputType(localBundle.getInt("input_type", -1));
		this.mEntry.setActivityType(localBundle.getInt("activity_type", -1));
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		int dialog_id=0;

		// Figuring out what dialog to show based on the position clicked

		switch (position) {
		case LIST_ITEM_ID_DATE:
			dialog_id = 2;      
			break;
		case LIST_ITEM_ID_TIME:
			dialog_id = 3;
			break;
		case LIST_ITEM_ID_DURATION:
			dialog_id = 4;
			break;
		case LIST_ITEM_ID_DISTANCE:
			dialog_id = 5;
			break;
		case LIST_ITEM_ID_CALORIES:
			dialog_id=6;
			break;
		case LIST_ITEM_ID_HEARTRATE:
			dialog_id=7;
			break;
		case LIST_ITEM_ID_COMMENT:
			dialog_id=8;
			break;
		default:
			dialog_id = MyRunsDialogFragment.DIALOG_ID_ERROR;
		}

		displayDialog(dialog_id);
	}


	//Display dialog based on id.
	public void displayDialog(int paramInt)
	{
		MyRunsDialogFragment.newInstance(paramInt).show(getFragmentManager(), getString(R.string.dialog_fragment_tag_general));
	}

	//"Save" button is clicked
	public void onSaveClicked(View paramView)
	{
		//Insert entry into DB
		long l = this.mEntry.insertToDB(this);

		//Pop up toast
		Toast.makeText(this, "Entry #" + l + " saved.", 0).show();

		// Close the activity
		finish();
	}


	//"Cancel button is clicked. Pop up toast and close activity	
	public void onCancelClicked(View paramView)
	{
		Toast.makeText(getApplicationContext(), "Entry discarded.", 0).show();
		finish();
	}

	// ********************************
	// The following are functions called after dialog is clicked.
	// Called from MyRunsDialogFragment side. mEntry is handled here in a  
	// cleaner and more separated way. 
	// value are parsed and set in mEntry for later database insertion.

	public void onCaloriesSet(String paramString)
	{
		int i;
		try
		{
			i = Integer.parseInt(paramString);
			this.mEntry.setCalories(i);
			return;
		}
		catch (NumberFormatException localNumberFormatException)
		{
			i = 0;
		}
	}

	public void onCommentSet(String paramString)
	{
		this.mEntry.setComment(paramString);
	}



	public void onDateSet(int year, int month, int day)
	{
		this.mEntry.setDate(year, month, day);
	}

	public void onDistanceSet(String paramString)
	{
		int i;
		try
		{
			double d = Double.parseDouble(paramString);
			i = (int)(1.609344D * (d * 1000.0D));
			this.mEntry.setDistance(i);
			return;
		}
		catch (NumberFormatException localNumberFormatException)
		{
			i = 0;
			}
	}

	public void onDurationSet(String paramString)
	{
		int i;
		try
		{
			double d = Double.parseDouble(paramString);
			i = (int)(d * 60.0D);
			this.mEntry.setDuration(i);
			return;
		}
		catch (NumberFormatException localNumberFormatException)
		{
			i = 0;
		}
	}

	public void onHeartrateSet(String paramString)
	{
		int i;
		try
		{
			i = Integer.parseInt(paramString);
			this.mEntry.setHeartrate(i);
			return;
		}
		catch (NumberFormatException localNumberFormatException)
		{
			i = 0;
		}
	}


	public void onTimeSet(int hour, int minute)
	{
		this.mEntry.setTime(hour, minute, 0);
		Log.w(ManualInputActivity.class.getName(), "time=entering the onTimeset");
	}
}


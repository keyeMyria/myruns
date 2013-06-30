package aaditya.myruns5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


//Display the details of a "manual" entry.
//All data are passed from the launching activity. Another way
//of doing it is only passing the entry id, and query the database in this activity.
//More work, but making the activity more self-contained.
public class DisplayEntryActivity extends Activity
{
	private static final int MENU_ID_DELETE = 0;

	private Context mContext;
	private long mId = 0;


	//Display all the columns in the saved entry once the activity is created
	@Override
	protected void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);

		//inflate the entry layout. 
		setContentView(R.layout.display_entry);

		this.mContext = this;

		//get extras into localBundle from HistroyTabFragment's intent
		Bundle localBundle = getIntent().getExtras();

		//display all the columns from the exercise entry to the list of TextView.
		if (localBundle != null)
		{
			this.mId = localBundle.getLong("_id");
			((EditText)findViewById(R.id.editDispActivityType)).setText(localBundle.getString("activity_type"));
			((EditText)findViewById(R.id.editDispDateTime)).setText(localBundle.getString("date_time"));
			((EditText)findViewById(R.id.editDispDuration)).setText(localBundle.getString("duration"));
			((EditText)findViewById(R.id.editDispDistance)).setText(localBundle.getString("distance"));
			((EditText)findViewById(R.id.editDispCalories)).setText(localBundle.getString("calories"));
			((EditText)findViewById(R.id.editDispHeartrate)).setText(localBundle.getString("heartrate"));
			((EditText)findViewById(R.id.editDispComment)).setText(localBundle.getString("comment"));
		}
	}

	//Create the option menu to delete the current saved exercise entry.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		MenuItem menuitem;
		menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE, "Delete");
		menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


		return true;
	}


	//When you click "delete" button, 
	//calls the deleteEntryInDB to delete this entry in the database and quit the activity.
	@Override
	public boolean onOptionsItemSelected(MenuItem paramMenuItem)
	{
		switch (paramMenuItem.getItemId())
		{
		case MENU_ID_DELETE:
			ExerciseEntryHelper.deleteEntryInDB(this.mContext, this.mId);
			finish();
			return true;
		default:
			finish();
			return false;
		
		}
	}
}


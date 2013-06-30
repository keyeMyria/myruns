package aaditya.myruns5;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;

public class MyRunsDialogFragment extends DialogFragment
{
	// Different dialog IDs
	public static final int DIALOG_ID_ERROR = -1;
	public static final int DIALOG_ID_PHOTO_PICKER = 1;
	public static final int DIALOG_ID_MANUAL_INPUT_DATE = 2;
	public static final int DIALOG_ID_MANUAL_INPUT_TIME = 3;
	public static final int DIALOG_ID_MANUAL_INPUT_DURATION = 4;
	public static final int DIALOG_ID_MANUAL_INPUT_DISTANCE = 5;
	public static final int DIALOG_ID_MANUAL_INPUT_CALORIES = 6;
	public static final int DIALOG_ID_MANUAL_INPUT_HEARTRATE = 7;
	public static final int DIALOG_ID_MANUAL_INPUT_COMMENT = 8;

	// For photo picker selection:
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;


	private static final String DIALOG_ID_KEY = "dialog_id";

	public static MyRunsDialogFragment newInstance(int dialog_id)
	{
		MyRunsDialogFragment fragment = new MyRunsDialogFragment();
		Bundle localBundle = new Bundle();
		localBundle.putInt(DIALOG_ID_KEY, dialog_id);
		//System.out.println ("Put Int " + dialog_id);
		fragment.setArguments(localBundle);
		return fragment;
	}
	public Dialog onCreateDialog(Bundle paramBundle)
	{
		int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
        //System.out.println("Dialog id: " + dialog_id);
		final Activity localActivity = getActivity();

		// For initializing date/time related dialogs
		final Calendar now = Calendar.getInstance();;
		
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DATE);

		// For text input field
		final EditText textEntryView = new EditText(localActivity);;


		switch (dialog_id)
		{
		case DIALOG_ID_PHOTO_PICKER:
			  AlertDialog.Builder localBuilder = new AlertDialog.Builder(localActivity);
			    localBuilder.setTitle(R.string.ui_profile_photo_picker_title);
			    localBuilder.setItems(R.array.ui_profile_photo_picker_items, new DialogInterface.OnClickListener()
			    {
			      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
			      {
			        ((ProfileActivity)localActivity).onPhotoPickerItemSelected(paramAnonymousInt);
			      }
			    });
			return localBuilder.create();
		case DIALOG_ID_MANUAL_INPUT_DATE:
			return new DatePickerDialog(localActivity, new DatePickerDialog.OnDateSetListener()
			{
				public void onDateSet(DatePicker paramAnonymousDatePicker, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
				{
					((ManualInputActivity)localActivity).onDateSet(paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3);
				}
			}
			, year, month, day);
		case DIALOG_ID_MANUAL_INPUT_TIME:

			return new TimePickerDialog(localActivity, new TimePickerDialog.OnTimeSetListener()
			{
				public void onTimeSet(TimePicker paramAnonymousTimePicker, int paramAnonymousInt1, int paramAnonymousInt2)
				{
					((ManualInputActivity)localActivity).onTimeSet(paramAnonymousInt1, paramAnonymousInt2);
				}
			}
			, hour, minute, false);
		case DIALOG_ID_MANUAL_INPUT_DURATION:
			textEntryView.setInputType(8194);
			return new AlertDialog.Builder(localActivity).setTitle(R.string.ui_manual_input_duration_title).setView(textEntryView).setPositiveButton(R.string.ui_button_ok_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					((ManualInputActivity)localActivity).onDurationSet(textEntryView.getText().toString());
				}
			}).setNegativeButton(R.string.ui_button_cancel_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					textEntryView.setText("");
				}
			}).create();
		case DIALOG_ID_MANUAL_INPUT_DISTANCE:
			textEntryView.setInputType(8194);
			return new AlertDialog.Builder(localActivity).setTitle(R.string.ui_manual_input_distance_in_miles_title).setView(textEntryView).setPositiveButton(R.string.ui_button_ok_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					((ManualInputActivity)localActivity).onDistanceSet(textEntryView.getText().toString());
				}
			}).setNegativeButton(R.string.ui_button_cancel_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					textEntryView.setText("");
				}
			}).create();
		case DIALOG_ID_MANUAL_INPUT_CALORIES:
			textEntryView.setInputType(2);
			return new AlertDialog.Builder(localActivity).setTitle(R.string.ui_manual_input_calories_title).setView(textEntryView).setPositiveButton(R.string.ui_button_ok_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					((ManualInputActivity)localActivity).onCaloriesSet(textEntryView.getText().toString());
				}
			}).setNegativeButton(R.string.ui_button_cancel_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					textEntryView.setText("");
				}
			}).create();
		case DIALOG_ID_MANUAL_INPUT_HEARTRATE:
			textEntryView.setInputType(2);
			return new AlertDialog.Builder(localActivity).setTitle(R.string.ui_manual_input_heartrate_title).setView(textEntryView).setPositiveButton(R.string.ui_button_ok_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					((ManualInputActivity)localActivity).onHeartrateSet(textEntryView.getText().toString());
				}
			}).setNegativeButton(R.string.ui_button_cancel_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					textEntryView.setText("");
				}
			}).create();
		case DIALOG_ID_MANUAL_INPUT_COMMENT:
			textEntryView.setInputType(1);
			textEntryView.setHint(R.string.ui_manual_input_comment_hint);
			textEntryView.setLines(4);
			return new AlertDialog.Builder(localActivity).setTitle(R.string.ui_manual_input_comment_title).setView(textEntryView).setPositiveButton(R.string.ui_button_ok_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					((ManualInputActivity)localActivity).onCommentSet(textEntryView.getText().toString());
				}
			}).setNegativeButton(R.string.ui_button_cancel_title, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
				{
					textEntryView.setText("");
				}
			}).create();
		default:
			return null;
		}
	}
}


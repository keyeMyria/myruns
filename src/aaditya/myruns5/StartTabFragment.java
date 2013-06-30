package aaditya.myruns5;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

public class StartTabFragment extends Fragment
{
	// Context stands for current running activity.

	private Context mContext;


	// View widgets on the screen needs to be programmatically configured 
	private Button mButtonStart;
	private Spinner mSpinnerActivityType;
	private Spinner mSpinnerInputType;

	@Override
	public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
	{
		// Inflate the view defined in start.xml. Return at the end 
		View localView = paramLayoutInflater.inflate(R.layout.start, paramViewGroup, false);

		// Initialize context 
		this.mContext = getActivity();

		// Initialize view widgets by IDs
		this.mSpinnerInputType = ((Spinner)localView.findViewById(R.id.spinnerInputType));
		this.mSpinnerActivityType = ((Spinner)localView.findViewById(R.id.spinnerActivityType));
		this.mButtonStart = ((Button)localView.findViewById(R.id.btnStart));

		// Setup the onClick event of the "Start" button
		this.mButtonStart.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View paramAnonymousView)
			{
				//Get the index of which input mode is selected. It can be:
				// "Manual" i.e. you type in everything, 
				// "GPS" i.e. using GPS track your route
				// "Automatic" i.e. using GPS plus automatic machine learning algorithm to detect your activity type. 

				int inputType = StartTabFragment.this.mSpinnerInputType.getSelectedItemPosition();

				// Create a local bundle for extras to store the index of item selected
				// and pass to the next activity through an intent.
				Bundle localBundle = new Bundle();
				
				// New task (versus reading form history database) 
				localBundle.putInt("TASK_TYPE", 1);


				// Input type as explained above.
				localBundle.putInt("input_type", inputType);

				Intent localIntent;

				// Based on different selection, start different activity with

				switch (inputType) {

				case Globals.INPUT_TYPE_MANUAL:

					// Manual input
					localBundle.putInt(Globals.KEY_ACTIVITY_TYPE,
							StartTabFragment.this.mSpinnerActivityType.getSelectedItemPosition());
					
					// Create Intent to launch ManualInputActivity
					 localIntent = new Intent(StartTabFragment.this.mContext, ManualInputActivity.class);
					 break;
					 
				case Globals.INPUT_TYPE_GPS:
					//GPS
					localBundle.putInt(Globals.KEY_ACTIVITY_TYPE,
							StartTabFragment.this.mSpinnerActivityType.getSelectedItemPosition());
					
					// Create Intent to launch MapDisplayActivity
					 localIntent = new Intent(StartTabFragment.this.mContext, MapDisplayActivity.class);
					 break;
				
                case Globals.INPUT_TYPE_AUTOMATIC:
                     localBundle.putInt(Globals.KEY_ACTIVITY_TYPE, -1);
					 localIntent = new Intent(StartTabFragment.this.mContext, MapDisplayActivity.class);
                     break;
				
                default:
					return;
				}

				// Put extras into the intent
				localIntent.putExtras(localBundle);
				
				// Launch activity
				startActivity(localIntent);

			}
		});
	
		return localView;
	}
}


package aaditya.myruns5;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

//The main activity of the application. 
//Three tab fragments reside in this activity.
public class MainActivity extends Activity
{
	private static final String TAB_INDEX_KEY = "tab_index";

	@Override
	protected void onCreate(Bundle paramBundle) {

		super.onCreate(paramBundle);

		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// 1st tab "Start"
		// Get the resource string for the title of the tab
		String str1 = getString(R.string.ui_main_tab_start_title);
		//Create a new tab and add it to the ActionBar
		actionBar.addTab(actionBar.newTab().setText(str1).setTabListener(new TabListener(this, str1, StartTabFragment.class)));

		// 2nd tab "History"
		String str2 = getString(R.string.ui_main_tab_history_title);
		actionBar.addTab(actionBar.newTab().setText(str2).setTabListener(new TabListener(this, str2, HistoryTabFragment.class)));

		// 3rd tab "Settings"
		String str3 = getString(R.string.ui_main_tab_settings_title);
		actionBar.addTab(actionBar.newTab().setText(str3).setTabListener(new TabListener(this, str3, SettingsTabFragment.class)));

		// Load the previously saved tab index before the activity goes into background 
		if (paramBundle != null)
			actionBar.setSelectedNavigationItem(paramBundle.getInt(TAB_INDEX_KEY, 0));
	}

	@Override
	// Save the tab index before the activity goes into background.
	protected void onSaveInstanceState(Bundle paramBundle)
	{
		super.onSaveInstanceState(paramBundle);
		paramBundle.putInt(TAB_INDEX_KEY, getActionBar().getSelectedNavigationIndex());
	}

	public static class TabListener<T extends Fragment>
	implements ActionBar.TabListener
	{
		private final Activity mActivity;
		private final Class<T> mClass;
		private Fragment mFragment;
		private final String mTag;

		public TabListener(Activity paramActivity, String paramString, Class<T> paramClass)
		{
			this.mActivity = paramActivity;
			this.mTag = paramString;
			this.mClass = paramClass;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.

			this.mFragment = this.mActivity.getFragmentManager().findFragmentByTag(this.mTag);
			if ((this.mFragment != null) && (!this.mFragment.isDetached()))
			{
				FragmentTransaction localFragmentTransaction = this.mActivity.getFragmentManager().beginTransaction();
				localFragmentTransaction.detach(this.mFragment);
				localFragmentTransaction.commit();
			}
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		// User selected the already selected tab. Usually do nothing.
		public void onTabReselected(ActionBar.Tab paramTab, FragmentTransaction paramFragmentTransaction)
		{
		}


		public void onTabSelected(ActionBar.Tab paramTab, FragmentTransaction paramFragmentTransaction)
		{
			// Check if the fragment is already initialized. If not, instantiate and add it to the activity
			if (this.mFragment == null)
			{
				this.mFragment = Fragment.instantiate(this.mActivity, this.mClass.getName());
				paramFragmentTransaction.add(android.R.id.content, this.mFragment, this.mTag);
				return;
			}
			// If it exists, simply attach it in order to show it
			paramFragmentTransaction.attach(this.mFragment);
		}

		// Detach the fragment on tab unselect, because another one is being attached
		public void onTabUnselected(ActionBar.Tab paramTab, FragmentTransaction paramFragmentTransaction)
		{
			if (this.mFragment != null)
				paramFragmentTransaction.detach(this.mFragment);
		}
	}
}


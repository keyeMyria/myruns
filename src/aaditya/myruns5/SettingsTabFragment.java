package aaditya.myruns5;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsTabFragment extends PreferenceFragment
{
	// when created, add preferences from xml resource
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.preference);
  }
}


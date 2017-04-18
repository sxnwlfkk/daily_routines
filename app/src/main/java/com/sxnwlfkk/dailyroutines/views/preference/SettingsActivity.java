package com.sxnwlfkk.dailyroutines.views.preference;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.sxnwlfkk.dailyroutines.R;

public class SettingsActivity extends Activity {

    // Keys
    public static final String VIBRATE_PREF_NAME = "settings_vibration";
    public static final String CLOCK_BEFORE_LOCKSCREEN = "settings_clock_show_before_lockscreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

    }
}

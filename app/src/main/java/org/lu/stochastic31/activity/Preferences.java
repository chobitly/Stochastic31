package org.lu.stochastic31.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.lu.stochastic31.R;

public class Preferences extends PreferenceActivity {
    public static final String PREFS_NAME = "stochastic31_preference";

    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, PREFS_NAME, MODE_PRIVATE,
                R.xml.preferences, true);
        // Load the preferences from an XML resource
        getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);
        // Display the fragment as the main content.
        // getFragmentManager().beginTransaction()
        // .replace(android.R.id.content, new PrefsFragment()).commit();
    }

    // public static class PrefsFragment extends PreferenceFragment {
    //
    // @Override
    // public void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    //
    // PreferenceManager.setDefaultValues(getActivity(), PREFS_NAME,
    // MODE_PRIVATE, R.xml.preferences, true);
    // // Load the preferences from an XML resource
    // getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
    // addPreferencesFromResource(R.xml.preferences);
    // }
    // }
}

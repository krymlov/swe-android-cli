package org.swisseph.app;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;


/**
 * @author Yura Krymlov
 * @version 1.0, 2021-08
 */
public final class MainFbFragment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("R.string.menu_app_help_and_fb");

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new MainFbFragment.PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(swisseph.R.xml.app_feedback, rootKey);
        }
    }
}
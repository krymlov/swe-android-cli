package org.swisseph.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import swisseph.R;


/**
 * @author Yura Krymlov
 * @version 1.0, 2020-03
 */
public class MainAppSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("R.string.menu_app_preferences");

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_settings, rootKey);
        }
    }
}

package org.swisseph;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import swisseph.R;


/**
 * @author Yura Krymlov
 * @version 1.0, 2021-08
 */
public final class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_about);

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new AboutActivity.PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(swisseph.R.xml.app_about, rootKey);
        }
    }
}
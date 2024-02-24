/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2021-08
 */

package org.holidates.energy.appui.prefers;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.holidates.app.act.MyAppCompatActivity;
import org.holidates.energy.ArcanaApp;
import org.holidates.energy.R;

/**
 * @author Yura Krymlov
 * @version 1.0, 2021-08
 */
public final class MainFbFragment extends MyAppCompatActivity<ArcanaApp> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(org.holidates.suppart.R.string.menu_app_help_and_fb);

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new MainFbFragment.PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_feedback, rootKey);
        }

        /*
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setBackgroundColor(getResources().getColor(R.color.white));
        }*/
    }
}
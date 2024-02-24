package org.holidates.energy.appui.prefers;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.S;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static org.holidates.app.ctx.MyAppDefaultPrefers.USER_SELECTED_DARK_MODE;
import static org.holidates.app.ctx.MyAppDefaultPrefers.USER_SELECTED_LANGUAGE;
import static org.holidates.energy.ArcanaApp.app;
import static org.holidates.energy.BuildConfig.DEBUG;
import static java.lang.Math.abs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.holidates.app.act.MyAppCompatActivity;
import org.holidates.energy.ArcanaApp;
import org.holidates.energy.R;

/**
 * @author Yura Krymlov
 * @version 1.0, 2020-03
 */
public class MainAppSettings extends MyAppCompatActivity<ArcanaApp> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(org.holidates.suppart.R.string.menu_app_preferences);

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new PreferenceFragment()).commit();
    }

    /**
     * @author Yura Krymlov
     * @version 1.0, 2020-03
     */
    public static class PreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_settings, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final boolean treeClick = super.onPreferenceTreeClick(preference);

            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                switch (pref.getKey()) {
                    case USER_SELECTED_LANGUAGE: {
                        turnLanguage(context(), newValue);
                        return true;
                    }

                    case USER_SELECTED_DARK_MODE: {
                        return turnDarkThemeMode(newValue);
                    }
                }

                return true;
            });

            return treeClick;
        }
    }

    static boolean turnDarkThemeMode(Object mode) {
        if (null == mode) setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        else setDefaultNightMode(Integer.parseInt(mode.toString()));
        return true;
    }

    static void turnLanguage(Context context, Object newLang) {
        PackageManager packageManager = context.getPackageManager();
        if (null == newLang || packageManager == null) return;

        prefers().setUserSelectedLanguage(newLang.toString());

        //IMyGCalPlace primaryPlace = getPlacesManager().getPrimaryPlace();
        //if (null != primaryPlace) getPlacesManager().deleteByName(primaryPlace.name());

        Intent startActivity = packageManager.getLaunchIntentForPackage(config().appProcess());
        if (startActivity == null) return;

        startActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = FLAG_UPDATE_CURRENT;
        if (SDK_INT >= S) flags |= FLAG_IMMUTABLE;

        PendingIntent mPendingIntent = PendingIntent.getActivity(context,
                abs(context.hashCode()), startActivity, flags);

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);

        if (DEBUG) {
            logger().appInfo(MainAppSettings.class, "User selected-language: "
                    + prefers().getUserSelectedLanguage());
        }

        System.exit(0);
    }

    @Override
    protected void onStop() {
        // refresh app's preferences
        app().forceAppRefresh();
        super.onStop();
    }
}

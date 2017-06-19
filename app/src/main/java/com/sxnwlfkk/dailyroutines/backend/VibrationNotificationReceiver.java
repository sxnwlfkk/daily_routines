package com.sxnwlfkk.dailyroutines.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by sxnwlfkk on 2017.06.19..
 */

public class VibrationNotificationReceiver extends BroadcastReceiver {

    // CONSTANTS

    public String LOG_TAG = VibrationNotificationReceiver.class.getSimpleName();

    public static final int VIBRATION_NO_PATTERN = 0;
    public static final int VIBRATION_END_ALARM = 1;
    public static final int VIBRATION_HALFTIME_ALARM = 2;
    public static final int VIBRATION_THIRD_ALARM = 3;
    public static final int VIBRATION_CARRY_ZERO = 4;
    public static final int VIBRATION_MAIN_ZERO = 5;

    public static final String VIBRATION_PATTERN = "vibration";

    // Vibration patterns
    public static final long[] END_PATTERN = {0, 50, 50, 50, 50, 50};
    public static final long[] LONG_PATTERN = {0, 50, 50, 50};
    public static final long[] SHORT_PATTERN = {0, 50};
    public static final long[] NO_PATTERN = {0};

    ArrayList<long[]> patterns = new ArrayList();

    public VibrationNotificationReceiver() {
        super();

        patterns.add(NO_PATTERN);
        patterns.add(END_PATTERN);
        patterns.add(LONG_PATTERN);
        patterns.add(SHORT_PATTERN);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Getting settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean vibrateSet = prefs.getBoolean(SettingsActivity.VIBRATE_PREF_NAME, true);

        Log.e(LOG_TAG, "Received a vibration.");

        if (vibrateSet) {
            // Getting the pattern, that should be played
            int patternType = intent.getIntExtra(VIBRATION_PATTERN, 0);
            int patternNum = 0;

            switch (patternType) {
                case VIBRATION_END_ALARM:
                    Log.e(LOG_TAG, "Received end vibration.");
                    patternNum = 1;
                    break;
                case VIBRATION_HALFTIME_ALARM:
                    Log.e(LOG_TAG, "Received halftime vibration.");
                    patternNum = 3;
                    break;
                case VIBRATION_THIRD_ALARM:
                    Log.e(LOG_TAG, "Received third vibration.");
                    patternNum = 3;
                    break;
                case VIBRATION_MAIN_ZERO:
                    Log.e(LOG_TAG, "Received main end vibration.");
                    patternNum = 2;
                    break;
                case VIBRATION_CARRY_ZERO:
                    Log.e(LOG_TAG, "Received carry end vibration.");
                    patternNum = 2;
                    break;
            }

            long[] pattern = patterns.get(patternNum);

            Vibrator vibr = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            vibr.vibrate(pattern, -1);
        }

    }
}

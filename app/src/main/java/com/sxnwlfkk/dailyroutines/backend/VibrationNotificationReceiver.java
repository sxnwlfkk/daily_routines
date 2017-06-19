package com.sxnwlfkk.dailyroutines.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by sxnwlfkk on 2017.06.19..
 */

public class VibrationNotificationReceiver extends BroadcastReceiver {

    // CONSTANTS
    public static final int VIBRATION_NO_PATTERN = 0;
    public static final int VIBRATION_END_ALARM = 1;
    public static final int VIBRATION_HALFTIME_ALARM = 2;
    public static final int VIBRATION_THIRD_ALARM = 2;
    public static final int VIBRATION_CARRY_ZERO = 3;
    public static final int VIBRATION_MAIN_ZERO = 3;

    public static final String VIBRATION_PATTERN = "vibration";

    // Vibration patterns
    long[] endPattern = {0, 50, 50, 50, 50, 50};
    long[] longPattern = {0, 50, 50, 50};
    long[] shortPattern = {0, 50};
    long[] noPattern = {0};

    ArrayList<long[]> patterns = new ArrayList();

    public VibrationNotificationReceiver() {
        super();

        patterns.add(noPattern);
        patterns.add(endPattern);
        patterns.add(longPattern);
        patterns.add(shortPattern);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Getting settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean notificationSet = prefs.getBoolean(SettingsActivity.VIBRATE_PREF_NAME, true);

        // Getting the pattern, that should be played
        int patternNumber = intent.getIntExtra(VIBRATION_PATTERN, 0);

        long[] pattern = patterns.get(patternNumber);

        Vibrator vibr = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibr.vibrate(pattern, -1);
    }
}

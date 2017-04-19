package com.sxnwlfkk.dailyroutines.backend;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.views.mainActivity.MainActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;

/**
 * Created by cs on 2017.04.19..
 */

public class BReceiver extends BroadcastReceiver {

    public static int DAY_IN_SECONDS = 24 * 60 * 60;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            // This is the alarm
//            onAlarm(context, intent);
        } else {
            // The system just booted
//            scheduleAlarms(context);
        }
    }

    private void onAlarm(Context ctx, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        boolean notificationSet = prefs.getBoolean(SettingsActivity.NOTIFICATION_PREF_NAME, true);
        if (notificationSet) {
            // Make the notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx.getApplicationContext())
                            .setSmallIcon(R.drawable.ic_info_black_24dp)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!");
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(ctx.getApplicationContext(), MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx.getApplicationContext());
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());

        }
        // Register next alarm with this intent
        registerNextAlarm(ctx, intent);
    }

    static void registerNextAlarm(Context ctx, Intent intent) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(ctx, BReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i, 0);

        mgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DAY_IN_SECONDS * 1000, pi);
    }

    static void scheduleAlarms(Context ctx) {

    }
}

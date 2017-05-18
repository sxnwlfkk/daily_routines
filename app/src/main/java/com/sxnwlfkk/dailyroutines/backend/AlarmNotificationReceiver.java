package com.sxnwlfkk.dailyroutines.backend;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.mainActivity.MainActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.Calendar;

/**
 * Created by cs on 2017.04.19..
 */

public class AlarmNotificationReceiver extends BroadcastReceiver {

    // Constant
    public static String ALARM_INTENT_LENGTH = "alarm_intent_length";
    public static String ALARM_INTENT_NAME = "alarm_intent_name";
    public static String ALARM_SETUP_WAS_DONE = "alarm_setup_was_done";

    public static int DAY_IN_SECONDS = 24 * 60 * 60;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            // This is the alarm
            onAlarm(context, intent);
        } else {
            // The system just booted
            scheduleAlarms(context);
        }
    }

    private void onAlarm(Context ctx, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        boolean notificationSet = prefs.getBoolean(SettingsActivity.NOTIFICATION_PREF_NAME, true);
        Uri currentUri = intent.getData();
        String routineName = intent.getStringExtra(ALARM_INTENT_NAME);
        if (notificationSet) {
            // Make the notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx.getApplicationContext())
                            .setSmallIcon(R.drawable.ic_stat_watch)
                            .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_stat_watch))
                            .setContentTitle(routineName)
                            .setAutoCancel(true)
                            .setContentText("If you start your routine, you will surely finish on time!");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(ctx.getApplicationContext(), ProfileActivity.class);
            resultIntent.setData(currentUri);

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
        int startTime = intent.getIntExtra(ALARM_INTENT_LENGTH, -1);
        if (startTime == -1) return;

        registerNextAlarm(ctx, currentUri, startTime, routineName);
    }

    public static void registerNextAlarm(Context ctx, Uri uri, int optimalTime, String name) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(ctx, AlarmNotificationReceiver.class);
        i.setData(uri);
        i.putExtra(ALARM_INTENT_LENGTH, optimalTime);
        i.putExtra(ALARM_INTENT_NAME, name);
        int id = (int) ContentUris.parseId(uri);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        int currTime = (hours * 3600) + (minutes * 60) + seconds;

        if (currTime + 5 > optimalTime) {
            int timeDifference = currTime - optimalTime;
            long nextTime = (System.currentTimeMillis() + DAY_IN_SECONDS * 1000) - timeDifference * 1000;
            mgr.setExact(AlarmManager.RTC_WAKEUP, nextTime, pi);
        } else {
            int futureTime = optimalTime - currTime;
            mgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + futureTime * 1000, pi);
        }

    }

    public static void cancelAlarm(Context ctx, Uri uri) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(ctx, AlarmNotificationReceiver.class);
        i.setData(uri);
        int id = (int) ContentUris.parseId(uri);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_CANCEL_CURRENT);
        mgr.cancel(pi);
    }

    public static void scheduleAlarms(Context ctx) {
        Log.e("BReceiver", "Scheduling alarms.");
        String[] projection = {
                RoutineContract.RoutineEntry._ID,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
            };

        Cursor cursor = ctx.getContentResolver().query(RoutineContract.RoutineEntry.CONTENT_URI, projection, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                int reqEnd = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END));
                if (reqEnd == 1) {
                    Log.e("Receiver", "Requires notification.");
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                    String routineName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                    int endTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
                    int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));

                    registerNextAlarm(ctx, ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, id), RoutineUtils.calculateIdealStartTime(endTime, length), routineName);
                }
            } while (cursor.moveToNext());
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        preferences.edit().putBoolean(ALARM_SETUP_WAS_DONE, true).apply();
    }
}

package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineClock;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cs on 2017.04.20..
 */

public class ClockService extends Service {

    // Command constants
    public static final int CLOCK_SERVICE_ROUTINE_START = 500;
    public static final int CLOCK_SERVICE_ROUTINE_CANCEL = 501;
    public static final int CLOCK_SERVICE_ROUTINE_FINISH = 502;
    public static final int CLOCK_SERVICE_NEXT_ITEM = 503;
    public static final int CLOCK_SERVICE_PREV_ITEM = 504;
    public static final int CLOCK_SERVICE_SEND_UPDATE = 505;
    public static final int CLOCK_SERVICE_STOP_TALKING = 506;

    // Intent field constants
    public static final String SERVICE_COMMAND_FIELD = "command";

    public static final String SERVICE_CURR_TIME_FIELD = "curr_time";
    public static final String SERVICE_CARRY_FIELD = "carry_time";
    public static final String SERVICE_CURR_ITEM_FIELD = "curr_item";
    public static final String SERVICE_SUM_ITEMS_FIELD = "sum_of_items";
    public static final String SERVICE_ITEM_NAME_FIELD = "item_name";
    public static final String SERVICE_ROUTINE_NAME_FIELD = "routine_name";

    public static final String LOG_TAG = ClockService.class.getSimpleName();

    public static String BROADCAST_ACTION = "com.sxnwlfkk.dailyroutines.clockUpdate";

    // VARS
    // TODO
    RoutineClock mRoutineClock;
    RoutineItem mCurrentItem;
    CountDownTimer mCountdownTimer;
    Uri mCurrentUri;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    PowerManager.WakeLock mWakelock;

    // Sentinel
    private boolean timerIsInitialised;
    private boolean shouldSpeak;
    private boolean routineIsSetUp;
    private boolean routineFinished;
    private boolean routineHasBennStarted;

    // Settings
    private boolean sVibrateOn;


    // On Create runs when initialising
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise variables
        mRoutineClock = new RoutineClock();
        timerIsInitialised = false;
        shouldSpeak = true;
        routineIsSetUp = false;
        routineFinished = false;
        mBuilder = null;
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        mWakelock.acquire();

        // Get settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sVibrateOn = prefs.getBoolean(SettingsActivity.VIBRATE_PREF_NAME, true);

        // Get notification manager
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancelAll();
        super.onDestroy();
        if (mWakelock.isHeld()) {
            mWakelock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Parse intent for command
        Log.e(LOG_TAG, "In service on start command.");

        int command = intent.getIntExtra(SERVICE_COMMAND_FIELD, 0);
        mCurrentUri = intent.getData();

        switch (command) {
            case CLOCK_SERVICE_ROUTINE_START:
                if (!routineIsSetUp) {
                    startRoutine();
                } else sendMessage();
                break;
            case CLOCK_SERVICE_NEXT_ITEM:
                nextItem();
                break;
            case CLOCK_SERVICE_PREV_ITEM:
                prevItem();
                break;
            case CLOCK_SERVICE_ROUTINE_CANCEL:
                cancelRoutine();
                break;
            case CLOCK_SERVICE_ROUTINE_FINISH:
                finishRoutine();
                break;
            case CLOCK_SERVICE_SEND_UPDATE:
                sendUpdate();
                break;
            case CLOCK_SERVICE_STOP_TALKING:
                stopTalking();
                break;
        }

        return START_REDELIVER_INTENT;
    }

    // Internal Commands

     private void getRoutineData() {
         // Select the appropriate behaviour for the Uri
         String[] projection = {
                 RoutineContract.RoutineEntry._ID,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY,
                 RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED,
                 RoutineContract.RoutineEntry.COLUMN_ROUTINE_INTERRUPT_TIME,
         };

         Cursor cursor = getContentResolver().query(mCurrentUri, projection, null, null, null);
         cursor.moveToFirst();

         long rId = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
         String rName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
         int rEndTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
         boolean rEndTimeReq = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END)) == 1;
         int rCarryTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY));
         int rCurrItem = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
         int rItemsNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));
         int rTimesUsed = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED));
         int rInterruptTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_INTERRUPT_TIME));

         // Set first start sentry
         if (rCurrItem == -1) {
             routineHasBennStarted = false;
         } else {
             routineHasBennStarted = true;
         }

         // Set these data to the clock object
         mRoutineClock.setmId(rId);
         mRoutineClock.setmName(rName);
         mRoutineClock.setmEndTime(rEndTime);
         mRoutineClock.setmEndTimeRequired(rEndTimeReq);
         mRoutineClock.setmCurrentItemIndex(rCurrItem);
         mRoutineClock.setmCarryTime(rCarryTime);
         mRoutineClock.setmRoutineItemsNum(rItemsNumber);
         mRoutineClock.setmTimesUsed(rTimesUsed);

         // Check diff time
         int rDiffTime = 0;
         if (rCurrItem > -1) {
             long currTime = System.currentTimeMillis() / 1000;
             rDiffTime = (int) currTime - rInterruptTime;
         }
         mRoutineClock.setmDiffTime(rDiffTime);
     }

     private void getItemsData(long id) {
         String[] projectionItems = new String[] {
                 RoutineContract.ItemEntry._ID,
                 RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
                 RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
                 RoutineContract.ItemEntry.COLUMN_ITEM_NO,
                 RoutineContract.ItemEntry.COLUMN_REMAINING_TIME,
                 RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
                 RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME,
                 RoutineContract.ItemEntry.COLUMN_START_TIME,
         };

         String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
         String[] selectionArgs = new String[] { String.valueOf(id) };
         Cursor cursor = getContentResolver().query(RoutineContract.ItemEntry.CONTENT_URI,
                 projectionItems, selection, selectionArgs,
                 RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");

         cursor.moveToFirst();

         ArrayList<RoutineItem> itemsList = new ArrayList<>();
         for (int i = 0; i < cursor.getCount(); i++) {
             long itemId = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));
             String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
             int itemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
             int itemAvgTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));
             int itemRemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME));
             int itemElapsedTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME));
             int itemStartTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_START_TIME));

             RoutineItem newItem = new RoutineItem(itemName, itemTime, itemAvgTime);
             newItem.setmId(itemId);
             newItem.setmCurrentTime(itemRemTime);
             newItem.setmElapsedTime(itemElapsedTime);
             newItem.setStartTime(itemStartTime);
             itemsList.add(newItem);
             if (!cursor.moveToNext()) break;
         }
         mRoutineClock.setmItemsList(itemsList);
     }

    // Read from DB
    private void queryDB() {
        // Calculate ID from Uri
        long id = 0;
        try {
            id = ContentUris.parseId(mCurrentUri);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid argument in URI (URI is not ending with .../#): " + mCurrentUri);
            return;
        }
        // Load data from DB
        getRoutineData();
        getItemsData(id);
    }

    // Load from DB and return first intent
    private void startRoutine() {
        Log.e(LOG_TAG, "Starting up or resuming routine service.");
        // Get data from DB
        queryDB();

        // First start of the routine
        if (!routineHasBennStarted) {
            if (mRoutineClock.ismEndTimeRequired()) {
                Calendar cal = Calendar.getInstance();
                int hours = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                int seconds = cal.get(Calendar.SECOND);
                int currTime = (hours * 3600) + (minutes * 60) + seconds;
                int optimalTime = RoutineUtils.calculateIdealStartTime(mRoutineClock.getmEndTime(), mRoutineClock.getmLength());
                int carry = optimalTime - currTime;

                int s;
                if (carry < 0) {
                    mRoutineClock.distributeCarryOnStart(carry);
                    mRoutineClock.setmLength(
                            ((s = mRoutineClock.getmLength() + carry) >= 0) ? s : 0
                    );
                } else if (carry >= 0) {
                    mRoutineClock.setmCarryTime(carry);
                    mRoutineClock.setmLength(mRoutineClock.getmLength() + carry);
                }
            }
            mRoutineClock.setStartTime();
        // Routine was started but the service was killed after
        } else {
            mRoutineClock.sortDiffTime();
        }

        mCurrentItem = mRoutineClock.getCurrentItem();
        if (!timerIsInitialised) {
            mCountdownTimer = new ClockCountdownTimer(mRoutineClock.getmLength() * 1000, 1000);
            mCountdownTimer.start();
            timerIsInitialised = true;
        }

        // Create routine notification and set service to foreground
        makeNotification();
        startForeground((int) mRoutineClock.getmId(), mBuilder.build());

        // Send back confirmation and basic data message
        sendMessage();
    }

    private void cancelRoutine() {
        Log.e(LOG_TAG, "In cancel routinte service.");
        mCountdownTimer.cancel();
        mWakelock.release();
        mRoutineClock.resetRoutine();
        writeRoutineToDB();
        stopForeground(true);
        mNotificationManager.cancel((int)mRoutineClock.getmId());
        mRoutineClock = new RoutineClock();
        timerIsInitialised = false;
        shouldSpeak = true;
        routineIsSetUp = false;
        routineFinished = false;
        mBuilder = null;
        stopSelf();
    }

    private void finishRoutine() {
        mCountdownTimer.cancel();
        mWakelock.release();
        mRoutineClock.finishRoutine();
        writeRoutineToDB();
        stopForeground(true);
        mNotificationManager.cancel((int)mRoutineClock.getmId());
        timerIsInitialised = false;
        shouldSpeak = true;
        routineIsSetUp = false;
        routineFinished = false;
        mBuilder = null;
        stopSelf();
    }

    private void nextItem() {
        mCurrentItem = mRoutineClock.nextItem(mCurrentItem);
        sendMessage();
    }

    private void prevItem() {
        mCurrentItem = mRoutineClock.prevItem(mCurrentItem);
        sendMessage();
    }

    private void sendUpdate() {
        sendMessage();
    }

    private void stopTalking() {
        mCountdownTimer.cancel();
        shouldSpeak = false;
    }

    private void sendMessage() {
        Intent message = new Intent(BROADCAST_ACTION);
        message.putExtra(SERVICE_ROUTINE_NAME_FIELD, mRoutineClock.getmName());
        message.putExtra(SERVICE_ITEM_NAME_FIELD, mCurrentItem.getmItemName());
        message.putExtra(SERVICE_SUM_ITEMS_FIELD, mRoutineClock.getmRoutineItemsNum());
        message.putExtra(SERVICE_CURR_ITEM_FIELD, mRoutineClock.getmCurrentItemIndex());
        message.putExtra(SERVICE_CURR_TIME_FIELD, mCurrentItem.getmCurrentTime());
        message.putExtra(SERVICE_CARRY_FIELD, mRoutineClock.getmCarryTime());
        sendBroadcast(message);
    }

    private void sendMessage(int currentItem) {
        Intent message = new Intent(BROADCAST_ACTION);
        message.putExtra(SERVICE_ROUTINE_NAME_FIELD, mRoutineClock.getmName());
        message.putExtra(SERVICE_ITEM_NAME_FIELD, mCurrentItem.getmItemName());
        message.putExtra(SERVICE_SUM_ITEMS_FIELD, mRoutineClock.getmRoutineItemsNum());
        message.putExtra(SERVICE_CURR_ITEM_FIELD, -1);
        message.putExtra(SERVICE_CURR_TIME_FIELD, mCurrentItem.getmCurrentTime());
        message.putExtra(SERVICE_CARRY_FIELD, mRoutineClock.getmCarryTime());
        sendBroadcast(message);

    }

    private void makeNotification() {
        // Make the notification
        String notificationText = "";
        if (!routineFinished) {
            notificationText = "Time in item: " + RoutineUtils.formatLengthString(mCurrentItem.getmCurrentTime())
                                + "Time left over: " + RoutineUtils.formatLengthString(mRoutineClock.getmCarryTime());
        } else {
            notificationText = "Time is up!";
        }

        if (mBuilder == null) {
            mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_stat_watch)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_watch))
                            .setContentTitle(mCurrentItem.getmItemName())
                            .setContentText(notificationText);
        }
        mBuilder.setContentTitle(mCurrentItem.getmItemName())
                .setContentText(notificationText);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getApplicationContext(), ClockActivity.class);
        resultIntent.setData(mCurrentUri);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ProfileActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        // mId allows you to update the notification later on.
        mNotificationManager.notify((int) mRoutineClock.getmId(), mBuilder.build());
    }

    private void writeRoutineToDB() {
        // Update routine
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM, mRoutineClock.getmCurrentItemIndex());
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY, mRoutineClock.getmCarryTime());
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED, mRoutineClock.getmTimesUsed());
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_INTERRUPT_TIME, System.currentTimeMillis() / 1000);
        getContentResolver().update(mCurrentUri, values, null, null);

        long updatedRoutineId = mRoutineClock.getmId();
        ArrayList<RoutineItem> itemsList = mRoutineClock.getmItemsList();
        // Update items
        for (int i = 0; i < mRoutineClock.getmRoutineItemsNum(); i++) {
            RoutineItem item = itemsList.get(i);
            Uri updateUri = ContentUris.withAppendedId(RoutineContract.ItemEntry.CONTENT_URI, item.getmId());
            ContentValues itemValues = new ContentValues();
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME, (int) item.getmAverageTime());
            Log.e(LOG_TAG, "Item average in double " + item.getmAverageTime() + " and in int: " + (int) item.getmAverageTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME, item.getmElapsedTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, item.getmCurrentTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_START_TIME, item.getStartTime());

            int rowsAffected = getContentResolver().update(updateUri, itemValues, null, null);
        }
    }

    private class ClockCountdownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public ClockCountdownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // Update clocks
            mCurrentItem.incrementElapsedTime();

            int currentItemTime = mCurrentItem.getmCurrentTime();
            if (currentItemTime == mCurrentItem.getStartTime() / 2 && currentItemTime != 0 && sVibrateOn) {
                Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibr.vibrate(50);
            } else if (currentItemTime == mCurrentItem.getStartTime() / 3 && currentItemTime != 0 && sVibrateOn) {
                Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibr.vibrate(50);
            }

            if (currentItemTime > 0) {
                if (currentItemTime == 1 && sVibrateOn) {
                    long[] pattern = {0, 50, 50, 50};
                    Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibr.vibrate(pattern, -1);
                }
                mCurrentItem.setmCurrentTime(currentItemTime - 1);
            } else {
                int carry = mRoutineClock.getmCarryTime();
                if (carry == 1 && sVibrateOn) {
                    long[] pattern = {0, 50, 50, 50};
                    Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibr.vibrate(pattern, -1);
                }
                mRoutineClock.setmCarryTime(carry - 1);
            }

            // Check if routine ended. Interrupt if timer would continue with no time left.
            if (mRoutineClock.getmCurrentItemIndex() + 1 == mRoutineClock.getmRoutineItemsNum()
                    && mRoutineClock.getmCarryTime() == 0
                    && mCurrentItem.getmCurrentTime() == 0) {
                onFinish();
            }

            // Send broadcast intent to Activity
            sendMessage();
            makeNotification();
            writeRoutineToDB();
        }

        @Override
        public void onFinish() {
            mCurrentItem.incrementElapsedTime();
            routineFinished = true;
            makeNotification();
            // Stop countdown
            if (sVibrateOn) {
                long[] pattern = {0, 50, 50, 50, 50, 50};
                Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibr.vibrate(pattern, -1);
            }

            final Timer finishTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (shouldSpeak) {
                        sendMessage(-1);
                    } else {
                        finishTimer.cancel();
                    }

                }
            };
            finishTimer.schedule(timerTask, 1000, 1000);
        }
    }

    // On Bind not used
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

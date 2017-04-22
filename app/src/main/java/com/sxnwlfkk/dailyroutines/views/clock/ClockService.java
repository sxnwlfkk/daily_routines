package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.LoaderManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sxnwlfkk.dailyroutines.classes.RoutineClock;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;

import java.util.ArrayList;
import java.util.Calendar;

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

    // Loader IDs
    private static final int CLOCK_ROUTINE_LOADER = 41;
    private static final int CLOCK_ITEM_LOADER = 42;
    private boolean otherLoaderFinished = false;

    // Sentinel
    private boolean timerIsInitialised;

    // Settings
    private boolean sVibrateOn;


    // TODO
    // On Create runs when initialising
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise variables
        mRoutineClock = new RoutineClock();
        timerIsInitialised = false;
    }

    // TODO
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Parse intent for command
        Log.e(LOG_TAG, "In service on start command.");

        int command = intent.getIntExtra(SERVICE_COMMAND_FIELD, 0);
        mCurrentUri = intent.getData();

        switch (command) {
            case CLOCK_SERVICE_ROUTINE_START:
                startRoutine();
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

        return START_NOT_STICKY;
    }

    // Internal Commands

    // Load from DB and return first intent
    private void startRoutine() {
        Log.e(LOG_TAG, "Starting up routine service.");
        long id = 0;
        try {
            id = ContentUris.parseId(mCurrentUri);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid argument in URI (URI is not ending with .../#): " + mCurrentUri);
            return;
        }

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

        int rDiffTime = 0;
        if (rCurrItem > -1) {
            long currTime = System.currentTimeMillis() / 1000;
            rDiffTime = (int) currTime - rInterruptTime;
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
        mRoutineClock.setmDiffTime(rDiffTime);

        // Items
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
        cursor = getContentResolver().query(RoutineContract.ItemEntry.CONTENT_URI,
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


        mRoutineClock.sortDiffTime();
        if (mRoutineClock.getmCurrentItemIndex() == 0
                && mRoutineClock.getCurrentItem().getmElapsedTime() == 0) {
            // Distribute carry time if needed
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
                } else if (carry > 0) {
                    mRoutineClock.setmCarryTime(carry);
                    mRoutineClock.setmLength(mRoutineClock.getmLength() + carry);
                }
            }
            // Set first start time
            mRoutineClock.setStartTime();
        }
        mCurrentItem = mRoutineClock.getCurrentItem();
        if (!timerIsInitialised) {
            mCountdownTimer = new ClockCountdownTimer(mRoutineClock.getmLength() * 1000, 1000);
            mCountdownTimer.start();
            timerIsInitialised = true;
        }

        // Send back confirmation and basic data message
        sendMessage();
    }

    private void cancelRoutine() {
        mCountdownTimer.cancel();
        mRoutineClock.resetRoutine();
        stopSelf();
    }

    private void finishRoutine() {
        mCountdownTimer.cancel();
        mRoutineClock.finishRoutine();
        writeRoutineToDB();
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
    // TODO
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

            // Send broadcast intent to Acitvity
            sendMessage();
        }

        @Override
        public void onFinish() {
            // Stop countdown

        }
    }

    // On Bind not used
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.LoaderManager;
import android.app.Service;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sxnwlfkk.dailyroutines.classes.RoutineClock;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;

/**
 * Created by cs on 2017.04.20..
 */

public class ClockService extends Service implements LoaderManager.LoaderCallbacks<Cursor> {

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
    CountDownTimer countDownTimer;
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
    // On Creatse runs when initialising
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise variables

        // Kick off loaders
    }

    // TODO
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Parse intent for command

        int command = 0;

        switch (command) {
            case CLOCK_SERVICE_NEXT_ITEM:
                nextItem();
                break;
            case CLOCK_SERVICE_PREV_ITEM:
                prevItem();
                break;
            case CLOCK_SERVICE_ROUTINE_START:
                startRoutine();
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
    private void startRoutine() {

    }

    private void cancelRoutine() {

        stopSelf();
    }

    private void finishRoutine() {

        stopSelf();
    }

    private void nextItem() {

    }

    private void prevItem() {

    }

    private void sendUpdate() {

    }

    private void stopTalking() {

    }

    // TODO
    // Loaderek
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    // TODO
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    // TODO
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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

            // Send broadcast intent to Acitvity
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

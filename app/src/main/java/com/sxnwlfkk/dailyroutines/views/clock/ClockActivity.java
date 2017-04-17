package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineClock;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.ArrayList;

public class ClockActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS
    private Uri mCurrentUri;

    // LOGTAG
    public static final String LOG_TAG = ClockActivity.class.getSimpleName();

    // Clock
    private RoutineClock mRoutineClock;

    // Current item
    private  RoutineItem mCurrentItem = null;

    // Clock sentinel
    private boolean usingCarryTimer = false;

    // Text color
    private int textColor;

    // Views
    private TextView mMainClockText;
    private TextView mCarryClockText;
    private TextView mItemNameText;
    private TextView mItemCounterText;

    // Buttons
    private Button mPreviousButton;
    private Button mNextButton;

    // On button click listeners

    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Sanity check
            if (mCurrentItem == null || mRoutineClock.getmCurrentItemIndex() + 1 >= mRoutineClock.getmRoutineItemsNum()) return;
            mCurrentItem = mRoutineClock.nextItem(mCurrentItem);
            refreshScreen();
        }
    };


    private View.OnClickListener previousButtonClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            // Sanity check
            if (mCurrentItem == null || mRoutineClock.getmCurrentItemIndex() - 1 < 0) return;

            mCurrentItem = mRoutineClock.prevItem(mCurrentItem);
            refreshScreen();
        }
    };

    // Countdown timer
    private ClockCountdownTimer mCountdownTimer;


    // Loader IDs
    private static final int CLOCK_ROUTINE_LOADER = 41;
    private static final int CLOCK_ITEM_LOADER = 42;
    private boolean otherLoaderFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get android to show this view before the lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_clock);

        // Initialize clock
        mRoutineClock = new RoutineClock();

        // Get URI
        mCurrentUri = getIntent().getData();

        // Menu bar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Load views
        mMainClockText = (TextView) findViewById(R.id.clock_main_clock);
        mCarryClockText = (TextView) findViewById(R.id.clock_carry_clock);
        textColor = mCarryClockText.getCurrentTextColor();
        mItemNameText = (TextView) findViewById(R.id.clock_item_name_text);
        mItemCounterText = (TextView) findViewById(R.id.clock_routine_item_counter_text);

        // Load buttons
        mPreviousButton = (Button) findViewById(R.id.clock_previous_button);
        mPreviousButton.setOnClickListener(previousButtonClickListener);
        mNextButton = (Button) findViewById(R.id.clock_next_button);
        mNextButton.setOnClickListener(nextButtonClickListener);

        // Start up Loaders
        getLoaderManager().initLoader(CLOCK_ROUTINE_LOADER, null, this);
        getLoaderManager().initLoader(CLOCK_ITEM_LOADER, null, this);
    }

    // Time and graphics methods
    private void refreshScreen() {
        updateClocks();
        mItemNameText.setText(mCurrentItem.getmItemName());
        mItemCounterText.setText("[" + (mRoutineClock.getmCurrentItemIndex() + 1) + "/"
                + mRoutineClock.getmRoutineItemsNum() + "]");
        mCarryClockText.setText(renderTime(mRoutineClock.getmCarryTime(), true));
    }

    private void updateClocks() {
        int currTime = mCurrentItem.getmCurrentTime();
        boolean canBeNegative = false;
        String clockText;
        if (currTime > 0) {
            mCarryClockText.setTextColor(textColor);
            clockText = renderTime(currTime, canBeNegative);
            mMainClockText.setText(clockText);
        } else {
            canBeNegative = true;
            if (mRoutineClock.getmCarryTime() < 0) {
                mCarryClockText.setTextColor(getResources().getColor(R.color.red));
            } else {
                mCarryClockText.setTextColor(textColor);
            }
            clockText = renderTime(mRoutineClock.getmCarryTime(), canBeNegative);
            mCarryClockText.setText(clockText);
            mMainClockText.setText("00:00");
        }
    }

    private String renderTime(int timeInSeconds, boolean canBeNegative) {
        String prefix = "";
        if (canBeNegative && timeInSeconds < 0) {
            prefix = "-";
            timeInSeconds *= -1;
        }
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String min = "";
        String sec = "";
        if (minutes < 10) min = "0";
        min += minutes;
        if (seconds < 10) sec = "0";
        sec += seconds;
        return prefix + min + ":" + sec;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clock_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStop() {
        Log.e(LOG_TAG, "In OnStop.");
        writeRoutineToDB();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                // TODO make popup window
                mCountdownTimer.cancel();
                mRoutineClock.resetRoutine();
                intent = new Intent(ClockActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                finish();
                return true;
            case R.id.clock_menu_finish:
                // TODO Make popup window
                mCountdownTimer.cancel();
                mRoutineClock.finishRoutine();
                intent = new Intent(ClockActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeRoutineToDB() {
        // Update routine
        ContentValues values = new ContentValues();
        Log.e(LOG_TAG, "Clock activity writing to DB. Current item's index is: " + mRoutineClock.getmCurrentItemIndex());
        values.put(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM, mRoutineClock.getmCurrentItemIndex());
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY, mRoutineClock.getmCarryTime());
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED, mRoutineClock.getmTimesUsed());
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

            int rowsAffected = getContentResolver().update(updateUri, itemValues, null, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // Try to get routine id
        long id = 0;
        try {
            id = ContentUris.parseId(mCurrentUri);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid argument in URI (URI is not ending with .../#): " + mCurrentUri);
            return null;
        }

        // Select the appropriate behaviour for the Uri
        switch (loaderId) {
            // Get the routine's main informations
            case CLOCK_ROUTINE_LOADER:
                String[] projection = {
                        RoutineContract.RoutineEntry._ID,
                        RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                        RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY,
                        RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM,
                        RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
                        RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER,
                        RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED,
                };

                return new CursorLoader(this,
                        mCurrentUri,
                        projection,
                        null,
                        null,
                        null);

            case CLOCK_ITEM_LOADER:
                // Get the routine's items
                String[] projectionItems = new String[] {
                        RoutineContract.ItemEntry._ID,
                        RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
                        RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
                        RoutineContract.ItemEntry.COLUMN_ITEM_NO,
                        RoutineContract.ItemEntry.COLUMN_REMAINING_TIME,
                        RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
                };

                String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
                String[] selectionArgs = new String[] { String.valueOf(id) };

                return new CursorLoader(this,
                        RoutineContract.ItemEntry.CONTENT_URI,
                        projectionItems,
                        selection,
                        selectionArgs,
                        RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        cursor.moveToFirst();

        switch (loaderId) {
            case CLOCK_ROUTINE_LOADER:
                long rId = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                String rName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                int rEndTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
                int rCarryTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY));
                int rCurrItem = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
                int rItemsNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));
                int rTimesUsed = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED));

                // Set these data to the clock object
                mRoutineClock.setmId(rId);
                mRoutineClock.setmName(rName);
                mRoutineClock.setmEndTime(rEndTime);
                mRoutineClock.setmCurrentItemIndex(rCurrItem);
                mRoutineClock.setmCarryTime(rCarryTime);
                mRoutineClock.setmRoutineItemsNum(rItemsNumber);
                mRoutineClock.setmTimesUsed(rTimesUsed);

                // Set action bar title
                getActionBar().setTitle(rName);
                // Set item counter
                mItemCounterText.setText("[" + (mRoutineClock.getmCurrentItemIndex() + 1) + "/" + mRoutineClock.getmRoutineItemsNum() + "]");

                if (otherLoaderFinished) {
                    bothLoaderFinished();
                }
                else otherLoaderFinished = true;
                break;
            case CLOCK_ITEM_LOADER:
                ArrayList<RoutineItem> itemsList = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    long id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));
                    String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
                    int itemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
                    int itemAvgTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));
                    int itemRemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME));

                    RoutineItem newItem = new RoutineItem(itemName, itemTime, itemAvgTime);
                    newItem.setmId(id);
                    newItem.setmCurrentTime(itemRemTime);
                    itemsList.add(newItem);
                    if (!cursor.moveToNext()) break;
                }
                mRoutineClock.setmItemsList(itemsList);

                if (otherLoaderFinished) {
                    bothLoaderFinished();
                }
                else otherLoaderFinished = true;
                break;
        }
    }

    private void bothLoaderFinished() {
        mCurrentItem = mRoutineClock.getCurrentItem();
        mItemNameText.setText(mCurrentItem.getmItemName());
        refreshScreen();
        mCountdownTimer = new ClockCountdownTimer(mRoutineClock.getmLength() * 1000, 1000);
        mCountdownTimer.start();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemCounterText.setText("[0/0]");
        mCarryClockText.setText("00:00");
        mMainClockText.setText("00:00");
        mItemNameText.setText("Item name");

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
            mRoutineClock.getCurrentItem().incrementElapsedTime();
            int currentItemTime = mCurrentItem.getmCurrentTime();
            if (currentItemTime > 0) {
                mCurrentItem.setmCurrentTime(currentItemTime - 1);
            } else {
                int carry = mRoutineClock.getmCarryTime();
                mRoutineClock.setmCarryTime(carry - 1);
            }
            updateClocks();
        }

        @Override
        public void onFinish() {
            // TODO
            // Do nothing for now
            mCountdownTimer.cancel();
            setContentView(R.layout.activity_clock_ending);
            Button discardButton = (Button) findViewById(R.id.clock_finished_discard_button);
            Button saveButton = (Button) findViewById(R.id.clock_finished_save_button);

            discardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRoutineClock.resetRoutine();
                    Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                    intent.setData(mCurrentUri);
                    startActivity(intent);
                    finish();
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRoutineClock.finishRoutine();
                    Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                    intent.setData(mCurrentUri);
                    startActivity(intent);
                    finish();

                }
            });
        }
    }
}


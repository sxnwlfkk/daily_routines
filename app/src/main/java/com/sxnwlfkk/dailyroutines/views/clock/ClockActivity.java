package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_SCREEN_OFF;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_NEXT_ITEM;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_PREV_ITEM;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_CANCEL;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_FINISH;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_START;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_SEND_UPDATE;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_STOP_TALKING;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.LOG_TAG;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CARRY_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_COMMAND_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CURR_ITEM_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CURR_TIME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ELAPSED_TIME;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ITEM_NAME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ROUTINE_LENGTH;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ROUTINE_NAME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_SUM_ITEMS_FIELD;

/**
 * Created by cs on 2017.04.20..
 */

public class ClockActivity extends Activity {

    // VARS
    // Text color
    private int textColor;

    Uri mCurrentUri;
    int mCurrentItem;
    int mSumOfItems;
    int mCarryTime;
    int mCurrentTime;
    long mElapsedTime;
    long mRoutineLength;
    String mItemName;
    String mRoutineName;

    // If screen refresh needed
    boolean waitingForScreenRefresh = true;

    // Views
    private TextView mMainClockText;
    private TextView mCarryClockText;
    private TextView mItemNameText;
    private TextView mItemCounterText;
    private ProgressBar mProgressBar;

    // Buttons
    private Button mPreviousButton;
    private Button mNextButton;

    // Settings
    private boolean sClockBeforeLockscreen;

    // Broadcast receiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get data from intent
            // Refresh the screen
            mItemName = intent.getStringExtra(SERVICE_ITEM_NAME_FIELD);
            mRoutineName = intent.getStringExtra(SERVICE_ROUTINE_NAME_FIELD);

            int c = 0;
            mCarryTime = intent.getIntExtra(SERVICE_CARRY_FIELD, 0);
            mCurrentTime = ((c = intent.getIntExtra(SERVICE_CURR_TIME_FIELD, -1)) != -1) ? c : mCurrentTime;
            mCurrentItem = c = intent.getIntExtra(SERVICE_CURR_ITEM_FIELD, -1);
            mSumOfItems = ((c = intent.getIntExtra(SERVICE_SUM_ITEMS_FIELD, -1)) != -1) ? c : mSumOfItems;
            mElapsedTime = intent.getLongExtra(SERVICE_ELAPSED_TIME, 0);
            mRoutineLength = intent.getLongExtra(SERVICE_ROUTINE_LENGTH, 0);

            if (mCurrentItem == -1) {
                Log.e(LOG_TAG, "Starting onFinish method.");
                onFinish();
            } else if (waitingForScreenRefresh) {
                refreshScreen();
                waitingForScreenRefresh = false;
            } else {
                updateClocks();
            }

        }
    };

    // Button click listeners
    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItem + 1 >= mSumOfItems) return;
            sendNextItemMessage();
            waitingForScreenRefresh = true;
        }
    };

    private View.OnClickListener previousButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Sanity check
            if (mCurrentItem - 1 < 0) return;

            if (mCurrentItem - 1 == 0) {
                mPreviousButton.setVisibility(View.GONE);
            }
            if (mCurrentItem == mSumOfItems - 1) {
                mNextButton.setText(R.string.routine_next_button);
                mNextButton.setOnClickListener(nextButtonClickListener);
            }
            sendPrevItemMessage();
            waitingForScreenRefresh = true;
        }
    };

    DialogInterface.OnClickListener finishListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendFinishRoutineMessage();
                                Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                                intent.setData(mCurrentUri);
                                startActivity(intent);
                                finish();
                            }
                        };

    // On create
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make intent and use it in startService(this, ClockService.class);
        Intent starterIntent = getIntent();
        mCurrentUri = starterIntent.getData();
        Intent serviceIntent = new Intent(this, ClockService.class);
        serviceIntent.setData(mCurrentUri);
        serviceIntent.putExtra(SERVICE_COMMAND_FIELD, CLOCK_SERVICE_ROUTINE_START);
        startService(serviceIntent);

        // Check settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sClockBeforeLockscreen = prefs.getBoolean(SettingsActivity.CLOCK_BEFORE_LOCKSCREEN_PREF_NAME, true);

        // Get android to show this view before the lockscreen
        if (sClockBeforeLockscreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.activity_clock);

        // Initialize values
        // Menu bar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Load views
        mMainClockText = (TextView) findViewById(R.id.clock_main_clock);
        mCarryClockText = (TextView) findViewById(R.id.clock_carry_clock);
        textColor = mCarryClockText.getCurrentTextColor();
        mItemNameText = (TextView) findViewById(R.id.clock_item_name_text);
        mItemCounterText = (TextView) findViewById(R.id.clock_routine_item_counter_text);
        mProgressBar = (ProgressBar) findViewById(R.id.clock_progressbar);

        // Load buttons
        mPreviousButton = (Button) findViewById(R.id.clock_previous_button);
        mPreviousButton.setOnClickListener(previousButtonClickListener);
        mNextButton = (Button) findViewById(R.id.clock_next_button);
        mNextButton.setOnClickListener(nextButtonClickListener);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ClockService.BROADCAST_ACTION);
        registerReceiver(mReceiver, filter);
        sendUpdateMessage();
        waitingForScreenRefresh = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }


    // On destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Dialogs
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.clock_cancel_message);
        builder.setPositiveButton(R.string.yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showFinishWithTimeRemainingDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.finish_with_time_remaining_msg);
        builder.setPositiveButton(R.string.yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Graphical methods
    private void refreshScreen() {
        updateClocks();

        if (mCurrentItem != 0) {
            mPreviousButton.setVisibility(View.VISIBLE);
        }

        if (mCurrentItem == mSumOfItems - 1) {
                mNextButton.setText(R.string.routine_finish_button);
                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener fListener = finishListener;
                        showFinishWithTimeRemainingDialog(fListener);
                    }
                });
        }

        mItemNameText.setText(mItemName);
        mItemCounterText.setText("[" + (mCurrentItem + 1) + "/"
                + mSumOfItems + "]");
        mCarryClockText.setText(renderTime(mCarryTime, true));
        getActionBar().setTitle(mRoutineName);

        if (mCurrentItem == 1) {
            mPreviousButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateClocks() {
        boolean canBeNegative = false;
        String clockText;
        if (mCurrentTime > 0) {
            mCarryClockText.setTextColor(textColor);
            clockText = renderTime(mCurrentTime, canBeNegative);
            mMainClockText.setText(clockText);
        } else {
            canBeNegative = true;
            clockText = renderTime(mCarryTime, canBeNegative);
            mCarryClockText.setText(clockText);
            mMainClockText.setText("00:00");
        }
        if (mCarryTime < 0) {
            mCarryClockText.setBackgroundColor(getResources().getColor(R.color.material_red_lighten1));
            mCarryClockText.setTextColor(getResources().getColor(R.color.white));
        } else if (mCarryTime > 0){
            mCarryClockText.setBackgroundColor(getResources().getColor(R.color.material_teal_lighten3));
            mCarryClockText.setTextColor(getResources().getColor(R.color.white));
        } else {
            mCarryClockText.setBackgroundColor(Color.TRANSPARENT);
            mCarryClockText.setTextColor(textColor);
        }
        updateProgressBar();
    }

    private void updateProgressBar() {
        int perc = 0;
        if (mRoutineLength != 0) {
            perc = (int) (((double) mElapsedTime / mRoutineLength) * 100);
            mProgressBar.setProgress(perc);
        } else {
            mProgressBar.setProgress(perc);
        }

    }

    private String renderTime(int timeInSeconds, boolean canBeNegative) {
        String prefix = "";
        if (canBeNegative && timeInSeconds < 0) {
            prefix = "-";
            timeInSeconds *= -1;
        }
        return prefix + RoutineUtils.formatCountdownTimeString(timeInSeconds);
    }

    // On finishing routine
    private void onFinish() {
        setContentView(R.layout.activity_clock_ending);

        Button discardButton = (Button) findViewById(R.id.clock_finished_discard_button);
        Button saveButton = (Button) findViewById(R.id.clock_finished_save_button);

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCancelRoutineMessage();
                Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                sendStopTalkingMessage();
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFinishRoutineMessage();
                Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                sendStopTalkingMessage();
                finish();
            }
        });
    }

    // Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clock_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCancelRoutineMessage();
                        Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                        intent.setData(mCurrentUri);
                        startActivity(intent);
                        finish();
                    }
                };

                showUnsavedChangesDialog(dismissListener);
                return true;
            case R.id.clock_menu_finish:
                DialogInterface.OnClickListener finishListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendFinishRoutineMessage();
                        Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                        intent.setData(mCurrentUri);
                        startActivity(intent);
                        finish();
                    }
                };
                showFinishWithTimeRemainingDialog(finishListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendCancelRoutineMessage();
                Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                finish();
            }
        };

        showUnsavedChangesDialog(dismissListener);
    }

    // Service communication

    private void sendServiceMessage(int command) {
        Intent newIntent = new Intent(this, ClockService.class);
        newIntent.setData(mCurrentUri);
        newIntent.putExtra(SERVICE_COMMAND_FIELD, command);
        startService(newIntent);

    }

    private void sendNextItemMessage() {
        sendServiceMessage(CLOCK_SERVICE_NEXT_ITEM);
    }

    private void sendPrevItemMessage() {
        sendServiceMessage(CLOCK_SERVICE_PREV_ITEM);
    }

    private void sendCancelRoutineMessage() {
        sendServiceMessage(CLOCK_SERVICE_ROUTINE_CANCEL);
    }

    private void sendFinishRoutineMessage() {
        sendServiceMessage(CLOCK_SERVICE_ROUTINE_FINISH);
    }

    private void sendUpdateMessage() {
        sendServiceMessage(CLOCK_SERVICE_SEND_UPDATE);
    }

    private void sendStopTalkingMessage() {
        sendServiceMessage(CLOCK_SERVICE_STOP_TALKING);
    }
}

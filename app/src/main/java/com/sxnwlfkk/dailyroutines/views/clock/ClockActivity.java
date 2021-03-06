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
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.util.RoutineUtils;
import com.sxnwlfkk.dailyroutines.views.mainActivity.MainActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_NEXT_ITEM;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_PREV_ITEM;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_CANCEL;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_FINISH;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_ROUTINE_START;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_SEND_UPDATE;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_STOP_TALKING;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.LOG_TAG;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CARRY_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CLOCK_FORCE_REFRESH;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_COMMAND_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CURR_ITEM_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_CURR_TIME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ELAPSED_TIME;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ITEM_NAME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ITEM_TIER;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ROUTINE_LENGTH;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_ROUTINE_NAME_FIELD;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_SUM_ITEMS_FIELD;

/**
 * Created by cs on 2017.04.20..
 */

public class ClockActivity extends Activity {

	Uri mCurrentUri;
	int mCurrentItem;
	int mSumOfItems;
	int mCarryTime;
	int mCurrentTime;
	int mItemTier;
	long mElapsedTime;
	long mRoutineLength;
	String mItemName;
	String mRoutineName;
	boolean routineEnded;
	// If screen refresh needed
	boolean waitingForScreenRefresh = true;
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
	// VARS
	// Text color
	private int textColor;
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
	// Button click listeners
	private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCurrentItem + 1 >= mSumOfItems) return;
			sendNextItemMessage();
			waitingForScreenRefresh = true;
		}
	};
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
			mItemTier = intent.getIntExtra(SERVICE_ITEM_TIER, 0);
			boolean forceRefresh = intent.getBooleanExtra(SERVICE_CLOCK_FORCE_REFRESH, false);

			if (mCurrentItem == -1) {
				Log.e(LOG_TAG, "Starting onFinish method.");
				onFinish();
			} else if (forceRefresh || waitingForScreenRefresh) {
				refreshScreen();
				waitingForScreenRefresh = false;
			} else {
				updateClocks();
			}

		}
	};
	private View.OnClickListener previousButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Sanity check
			if (mCurrentItem - 1 < 0) return;

			previousButtonCheck();
			sendPrevItemMessage();
			waitingForScreenRefresh = true;
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
		routineEnded = false;

		// Check settings
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		sClockBeforeLockscreen = prefs.getBoolean(SettingsActivity.CLOCK_BEFORE_LOCKSCREEN_PREF_NAME, true);

		// Get android to show this view before the lockscreen
		if (sClockBeforeLockscreen) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
					                     WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
					                     WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
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
	private void buttonChecks() {
		previousButtonCheck();
		nextButtonCheck();
		if (mCurrentItem != 0 || mCurrentItem == 1) {
			mPreviousButton.setVisibility(View.VISIBLE);
		}
	}

	private void nextButtonCheck() {
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
	}

	private void previousButtonCheck() {
		// If we are on the 0th item, make back button invisible
		if (mCurrentItem == 0) {
			mPreviousButton.setVisibility(View.GONE);
		}
		// If we are on the second to last item, switch finish button to next button
		if (mCurrentItem == mSumOfItems - 2) {
			mNextButton.setText(R.string.routine_next_button);
			mNextButton.setOnClickListener(nextButtonClickListener);
		}
	}

	private void refreshScreen() {
		updateClocks();

		buttonChecks();

		mItemNameText.setText(mItemName);
		mItemCounterText.setText("[" + (mCurrentItem + 1) + "/"
				                         + mSumOfItems + "]");
		mCarryClockText.setText(renderTime(mCarryTime, true));
		getActionBar().setTitle(mRoutineName);
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
		} else if (mCarryTime > 0) {
			mCarryClockText.setBackgroundColor(getResources().getColor(R.color.material_teal_lighten3));
			mCarryClockText.setTextColor(getResources().getColor(R.color.white));
		} else {
			mCarryClockText.setBackgroundColor(Color.TRANSPARENT);
			mCarryClockText.setTextColor(textColor);
		}
		updateProgressBar();
		setItemTierBackground();
	}

	private void setItemTierBackground() {
		int tier = mItemTier % 8;
		if (mItemTier == 0) {
			mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.bpTransparent));
		} else {
			switch (tier) {
				case 1:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier1));
					break;
				case 2:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier2));
					break;
				case 3:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier3));
					break;
				case 4:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier4));
					break;
				case 5:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier5));
					break;
				case 6:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier6));
					break;
				case 7:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier7));
					break;
				case 0:
					mItemNameText.setBackgroundColor(this.getResources().getColor(R.color.material_tier8));
					break;
			}
		}
	}

	private void updateProgressBar() {
		int perc = 0;
		if (mRoutineLength != 0) {
			perc = (int) (((double) mElapsedTime / mRoutineLength) * 100);
			mProgressBar.setProgress(perc);
			if (perc <= 50) {
				mProgressBar.getProgressDrawable().setColorFilter(
						getResources().getColor(R.color.material_indigo_darken3),
						PorterDuff.Mode.SRC_IN);
			} else if (perc < 90 && perc > 50) {
				mProgressBar.getProgressDrawable().setColorFilter(
						getResources().getColor(R.color.material_teal_lighten3),
						PorterDuff.Mode.SRC_IN);
			} else {
				mProgressBar.getProgressDrawable().setColorFilter(
						getResources().getColor(R.color.material_red_lighten1),
						PorterDuff.Mode.SRC_IN);
			}
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
		routineEnded = true;
		setContentView(R.layout.activity_clock_ending);
		sendStopTalkingMessage();

		Button discardButton = (Button) findViewById(R.id.clock_finished_discard_button);
		Button saveButton = (Button) findViewById(R.id.clock_finished_save_button);

		discardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendCancelRoutineMessage();
				Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
				intent.setData(mCurrentUri);
				startActivity(intent);
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
		DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendStopTalkingMessage();
				sendCancelRoutineMessage();
				Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
				intent.setData(mCurrentUri);
				startActivity(intent);
				finish();
			}
		};
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				if (!routineEnded) {
					Intent i = new Intent(this, MainActivity.class);
					navigateUpTo(i);
				} else {
					showUnsavedChangesDialog(dismissListener);
				}
				return true;
			case R.id.clock_menu_cancel:
				showUnsavedChangesDialog(dismissListener);
				return true;
			case R.id.clock_menu_finish:
				DialogInterface.OnClickListener finishListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendStopTalkingMessage();
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
		if (!routineEnded) {
			Intent i = new Intent(this, MainActivity.class);
			navigateUpTo(i);
		} else {
			DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sendStopTalkingMessage();
					sendCancelRoutineMessage();
					Intent intent = new Intent(ClockActivity.this, ProfileActivity.class);
					intent.setData(mCurrentUri);
					startActivity(intent);
					finish();
				}
			};

			showUnsavedChangesDialog(dismissListener);
		}
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

package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.TaskStackBuilder;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.ScreenOnOffReceiver;
import com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver;
import com.sxnwlfkk.dailyroutines.classes.RoutineClock;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.util.CompositionUtils;
import com.sxnwlfkk.dailyroutines.util.RoutineUtils;
import com.sxnwlfkk.dailyroutines.views.mainActivity.MainActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.app.NotificationCompat.Builder;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.END_PATTERN;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.LONG_PATTERN;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.NO_PATTERN;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.SHORT_PATTERN;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_CARRY_ZERO;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_END_ALARM;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_HALFTIME_ALARM;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_MAIN_ZERO;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_PATTERN;
import static com.sxnwlfkk.dailyroutines.backend.VibrationNotificationReceiver.VIBRATION_THIRD_ALARM;

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
	public static final int CLOCK_SERVICE_SCREEN_OFF = 507;
	public static final int CLOCK_SERVICE_SCREEN_ON = 508;
	// Intent field constants
	public static final String SERVICE_COMMAND_FIELD = "command";
	public static final String SERVICE_CURR_TIME_FIELD = "curr_time";
	public static final String SERVICE_CARRY_FIELD = "carry_time";
	public static final String SERVICE_CURR_ITEM_FIELD = "curr_item";
	public static final String SERVICE_SUM_ITEMS_FIELD = "sum_of_items";
	public static final String SERVICE_ITEM_NAME_FIELD = "item_name";
	public static final String SERVICE_ROUTINE_NAME_FIELD = "routine_name";
	public static final String SERVICE_ROUTINE_LENGTH = "routine_length";
	public static final String SERVICE_ELAPSED_TIME = "elapsed_time";
	public static final String SERVICE_CLOCK_FORCE_REFRESH = "force_refresh";
	public static final String SERVICE_ITEM_TIER = "item_tier_message";
	// Namestring of preference in the service
	public static final String SERVICE_PREFERENCE_LENGTH_WHEN_STARTED = "length_when_started";
	public static final String SERVICE_PREFERENCE_INTERRUPT_TIME = "interrupt_time";
	public static final String CLOCK_ROUTINE_IN_PROGRESS = "routine_in_progress";
	// Countdown interval constant
	public static final long COUNTDOWN_INTERVAL_CONST = 1000;
	public static final String LOG_TAG = ClockService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 479284712;
	private static final String CHANNEL_ID = "daily_routines_notification_channel";
	// Step correction constant
	private static final long STEP_CORRECTION_CONST = 0;
	public static String BROADCAST_ACTION = "com.sxnwlfkk.dailyroutines.clockUpdate";

	// VARS
	RoutineClock mRoutineClock;
	RoutineItem mCurrentItem;
	CountDownTimer mCountdownTimer;
	Timer mRepeatEndMessageTimer;
	Uri mCurrentUri;
	NotificationManager mNotificationManager;
	Builder mBuilder;
	BroadcastReceiver mReceiver;
	SharedPreferences mPrefs;

	long mRoutineLengthWhenStarted;

	boolean shouldVibrateInServiceNext;
	long[] inServiceVibrationPattern;

	// Sentinel
	private boolean timerIsInitialised;
	private boolean shouldSpeak;
	private boolean routineIsSetUp;
	private boolean routineFinished;
	private boolean routineHasBeenStarted;
	private boolean mScreenIsOn;
	private boolean justVibrated;
	private boolean previouslyVibrated;

	// Settings
	private boolean sVibrateOn;


	// On Create runs when initialising
	@Override
	public void onCreate() {
		super.onCreate();


		// Initialise variables
		mRoutineClock = new RoutineClock();
		mRepeatEndMessageTimer = null;
		timerIsInitialised = false;
		shouldSpeak = true;
		routineIsSetUp = false;
		routineFinished = false;
		mBuilder = null;
		mScreenIsOn = true;
		shouldVibrateInServiceNext = false;
		justVibrated = false;
		previouslyVibrated = false;
		inServiceVibrationPattern = NO_PATTERN;

		// Get settings
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		sVibrateOn = mPrefs.getBoolean(SettingsActivity.VIBRATE_PREF_NAME, true);
		mRoutineLengthWhenStarted = mPrefs.getLong(SERVICE_PREFERENCE_LENGTH_WHEN_STARTED, 0);

		// Get notification manager
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Get screen events receiver
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenOnOffReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		mNotificationManager.cancelAll();

		if (mReceiver != null)
			unregisterReceiver(mReceiver);

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Parse intent for command
		Log.e(LOG_TAG, "In service on start command.");

		int command = intent.getIntExtra(SERVICE_COMMAND_FIELD, 0);
		if (intent.getData() != null) {
			mCurrentUri = intent.getData();
		}

		switch (command) {
			case CLOCK_SERVICE_ROUTINE_START:
				Log.e(LOG_TAG, "Got command: START.");
				if (!routineIsSetUp) {
					startRoutine();
				} else sendMessage();
				break;
			case CLOCK_SERVICE_NEXT_ITEM:
				Log.e(LOG_TAG, "Got command: NEXT.");
				nextItem();
				break;
			case CLOCK_SERVICE_PREV_ITEM:
				Log.e(LOG_TAG, "Got command: PREV.");
				prevItem();
				break;
			case CLOCK_SERVICE_ROUTINE_CANCEL:
				Log.e(LOG_TAG, "Got command: CANCEL.");
				cancelRoutine();
				break;
			case CLOCK_SERVICE_ROUTINE_FINISH:
				Log.e(LOG_TAG, "Got command: FINISH.");
				finishRoutine();
				break;
			case CLOCK_SERVICE_SEND_UPDATE:
				Log.e(LOG_TAG, "Got command: UPDATE.");
				sendUpdate();
				break;
			case CLOCK_SERVICE_SCREEN_OFF:
				Log.e(LOG_TAG, "Got command: SCREEN OFF.");
				clockScreenOff();
				break;
			case CLOCK_SERVICE_SCREEN_ON:
				Log.e(LOG_TAG, "Got command: SCREEN ON.");
				clockScreenOn();
				sendUpdate();
				break;
			case CLOCK_SERVICE_STOP_TALKING:
				Log.e(LOG_TAG, "Got command: STOP.");
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

		// Set first start sentry
		if (rCurrItem == -1) {
			routineHasBeenStarted = false;
		} else {
			routineHasBeenStarted = true;
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
		mRoutineClock.setmInterruptTime(readInterruptTime());

		// Check diff time
		long rDiffTime = 0;
		if (rCurrItem > -1) {
			long currTime = System.currentTimeMillis();
			Log.d(LOG_TAG, "Current time in millis: " + currTime);
			Log.d(LOG_TAG, "Interrupt time in millis: " + mRoutineClock.getmInterruptTime());
			rDiffTime = currTime - mRoutineClock.getmInterruptTime();
			Log.d(LOG_TAG, "Diff time in millis: " + rDiffTime);
		}
		mRoutineClock.setmDiffTime(rDiffTime);
		cursor.close();
	}

	private void getItemsData(long id) {
		String[] projectionItems = new String[]{
				RoutineContract.ItemEntry._ID,
				RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE,
				RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
				RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
				RoutineContract.ItemEntry.COLUMN_ITEM_NO,
				RoutineContract.ItemEntry.COLUMN_REMAINING_TIME,
				RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
				RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME,
				RoutineContract.ItemEntry.COLUMN_START_TIME,
		};

		String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
		String[] selectionArgs = new String[]{String.valueOf(id)};
		Cursor cursor = getContentResolver().query(RoutineContract.ItemEntry.CONTENT_URI,
				projectionItems, selection, selectionArgs,
				RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");


		cursor.moveToFirst();

		ArrayList<RoutineItem> itemsList = CompositionUtils.composeRoutine(this.getBaseContext(), cursor);
		mRoutineClock.setmItemsList(itemsList);
		mRoutineClock.setmRoutineItemsNum(itemsList.size());
		cursor.close();
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
		// Get data from DB
		queryDB();

		// Kill all previous item vibrations
		cancelItemVibrations();

		// First start of the routine
		if (!routineHasBeenStarted) {
			if (mRoutineClock.ismEndTimeRequired()) {
				updateInterrupt(System.currentTimeMillis());
				int currTime = RoutineUtils.getCurrentTimeInSec();
				Log.e(LOG_TAG, "Current time is: " + currTime);
				int optimalTime = RoutineUtils.calculateIdealStartTime(
						RoutineUtils.msecToSec(mRoutineClock.getmEndTime()),
						RoutineUtils.msecToSec(mRoutineClock.getmLength()));
				Log.e(LOG_TAG, "Optimal start time is: " + optimalTime);
				long carry = RoutineUtils.secToMsec(optimalTime - currTime);
				Log.e(LOG_TAG, "Carry time at start is: " + carry);

				if (carry < 0) {
					mRoutineClock.distributeCarryOnStart(carry);
					mRoutineClock.calculateRemainingRoutineTime();
				} else {
					mRoutineClock.setmCarryTime(carry);
					mRoutineClock.setmLength(mRoutineClock.getmLength() + carry);
				}
			} else {
				mRoutineClock.setmEndTime(calculateEndTime());
			}
			mRoutineClock.setStartTime();
			routineHasBeenStarted = true;
			updateInterrupt(System.currentTimeMillis());
			// Routine was started but the service was killed after
		} else {
			mRoutineClock.sortDiffTime();
		}


		mCurrentItem = mRoutineClock.getCurrentItem();
		if (!timerIsInitialised) {
			Log.e(LOG_TAG, "Setting countdown in the future: " + mRoutineClock.getmLength());
			startCountdownTimer(mRoutineClock.getmLength(), COUNTDOWN_INTERVAL_CONST);
			if (mRoutineLengthWhenStarted == 0) {
				mRoutineLengthWhenStarted = mRoutineClock.getmLength();
				updateLengthPref(mRoutineLengthWhenStarted);
			}
			mRoutineClock.calculateElapsedTime();
			timerIsInitialised = true;
		}

		// Create routine notification and set service to foreground
		makeNotification();

		// Start in foreground mode
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
			startMyOwnForeground(mBuilder);
		else
			startForeground((int) mRoutineClock.getmId(), mBuilder.build());

		// Register vibrations
		registerEndVibration();
		registerItemVibrations();

		// Send back confirmation and basic data message
		sendMessage();

		setStartedRoutine();
	}

	private void cancelRoutine() {
		Log.e(LOG_TAG, "In cancel routine service.");
		if (mCountdownTimer != null) {
			mCountdownTimer.cancel();
			mCountdownTimer = null;
		}
		updateInterrupt(0);
		mRoutineClock.resetRoutine();
		writeRoutineToDB();
		stopForeground(true);
		mNotificationManager.cancel((int) mRoutineClock.getmId());
		mRoutineClock = null;
		timerIsInitialised = false;
		if (mRepeatEndMessageTimer != null) {
			mRepeatEndMessageTimer.cancel();
			mRepeatEndMessageTimer = null;
		}
		shouldSpeak = true;
		routineIsSetUp = false;
		routineFinished = false;
		mBuilder = null;
		updateLengthPref(0);
		cancelItemVibrations();
		cancelEndVibration();
		updateLengthPref(0);
		setFinishedRoutine();
		stopSelf();
	}

	private void finishRoutine() {
		if (mCountdownTimer != null) {
			mCountdownTimer.cancel();
			mCountdownTimer = null;
		}
		updateInterrupt(0);
		mRoutineClock.finishRoutine();
		writeRoutineToDB();
		stopForeground(true);
		mNotificationManager.cancel((int) mRoutineClock.getmId());
		mRoutineClock = null;
		timerIsInitialised = false;
		if (mRepeatEndMessageTimer != null) {
			mRepeatEndMessageTimer.cancel();
			mRepeatEndMessageTimer = null;
		}
		shouldSpeak = true;
		routineIsSetUp = false;
		routineFinished = false;
		mBuilder = null;
		cancelItemVibrations();
		cancelEndVibration();
		updateLengthPref(0);
		setFinishedRoutine();
		stopSelf();
	}

	// This method tries to mitigate the time surplus gained by not subtracting anything when
	// pushing prev or next.
	private void stepTimeCorrection(String direction) {
		if (direction.equals("next")) {
			long currTime = mCurrentItem.getmCurrentTime();
			if (currTime > STEP_CORRECTION_CONST) {
				mCurrentItem.setmCurrentTime(currTime - STEP_CORRECTION_CONST);
			} else {
				mRoutineClock.setmCarryTime(mRoutineClock.getmCarryTime() - STEP_CORRECTION_CONST);
			}
		} else if (direction.equals("prev")) {
			mRoutineClock.setmCarryTime(mRoutineClock.getmCarryTime() - STEP_CORRECTION_CONST);
		}
	}

	private void nextItem() {
		if (mRoutineClock.getmCurrentItemIndex() < mRoutineClock.getmRoutineItemsNum() - 1) {
			stepTimeCorrection("next");
			mCurrentItem = mRoutineClock.nextItem(mCurrentItem);
			cancelItemVibrations();
			registerItemVibrations();
			sendMessageForcedRefresh();
		}
	}

	private void prevItem() {
		if (mRoutineClock.getmCurrentItemIndex() > 0) {
			stepTimeCorrection("prev");
			mCurrentItem = mRoutineClock.prevItem(mCurrentItem);
			cancelItemVibrations();
			registerItemVibrations();
			sendMessageForcedRefresh();
		}
	}

	private void clockScreenOn() {
		if (routineHasBeenStarted) {
			checkDiffTime();
			mScreenIsOn = true;
			long interrupt = System.currentTimeMillis();
			updateInterrupt(interrupt);
		}
	}

	private void clockScreenOff() {
		PowerManager.WakeLock wl = getWakeLock();
		wl.acquire(10 * 60 * 1000L /*10 minutes*/);
		if (routineHasBeenStarted) {
			mScreenIsOn = false;
			writeRoutineToDB();
//            updateInterrupt(System.currentTimeMillis());
		}
		wl.release();
	}

	private void checkDiffTime() {
		// Check diff time
		if (mRoutineClock.getmInterruptTime() != 0) {
			long rDiffTime = System.currentTimeMillis() - mRoutineClock.getmInterruptTime();
			Log.e(LOG_TAG, "Diff time: " + rDiffTime);
			mRoutineClock.setmDiffTime(rDiffTime);
			mRoutineClock.sortDiffTime();
		}
	}

	private void sendUpdate() {
		sendMessage();
	}

	private void stopTalking() {
		if (mCountdownTimer != null) {
			mCountdownTimer.cancel();
		}
		shouldSpeak = false;
	}

	private Intent getBasicMessageIntent() {
		Intent message = new Intent(BROADCAST_ACTION);

		if (mRoutineClock.getmCurrentItemIndex() + 1 == mRoutineClock.getmRoutineItemsNum()
				    && mCurrentItem.getmCurrentTime() == 0
				    && mRoutineClock.getmCarryTime() <= 0) {
			message.putExtra(SERVICE_CARRY_FIELD, 0);
		} else {
			message.putExtra(SERVICE_CARRY_FIELD, RoutineUtils.msecToSec(mRoutineClock.getmCarryTime()));
		}

		message.putExtra(SERVICE_ROUTINE_NAME_FIELD, mRoutineClock.getmName());
		message.putExtra(SERVICE_ITEM_NAME_FIELD, mCurrentItem.getmItemName());
		message.putExtra(SERVICE_ITEM_TIER, mCurrentItem.getmTier());
		message.putExtra(SERVICE_SUM_ITEMS_FIELD, mRoutineClock.getmRoutineItemsNum());
		message.putExtra(SERVICE_CURR_TIME_FIELD, RoutineUtils.msecToSec(mCurrentItem.getmCurrentTime()));
		message.putExtra(SERVICE_ROUTINE_LENGTH, mRoutineLengthWhenStarted);
		message.putExtra(SERVICE_ELAPSED_TIME, mRoutineClock.getmElapsedTime());
		Log.d(LOG_TAG, "Routine length: " + mRoutineLengthWhenStarted);
		Log.d(LOG_TAG, "Elapsed time: " + mRoutineClock.getmElapsedTime());

		return message;
	}

	// General message containing basic information about the current state of the routine
	private void sendMessage() {
		Intent message = getBasicMessageIntent();
		message.putExtra(SERVICE_CURR_ITEM_FIELD, mRoutineClock.getmCurrentItemIndex());
		sendBroadcast(message);
	}

	// Sends a general message but with the forceRefresh flag, to elicit refresh started from the
	// service. Used when previous or next command was issued from the notification.
	private void sendMessageForcedRefresh() {
		Intent message = getBasicMessageIntent();
		message.putExtra(SERVICE_CURR_ITEM_FIELD, mRoutineClock.getmCurrentItemIndex());
		message.putExtra(SERVICE_CLOCK_FORCE_REFRESH, true);
		sendBroadcast(message);
	}

	// Sends an update message to the view with a specified item number
	// Currently used for signaling the end of the routine to the view
	private void sendMessage(int currentItem) {
		Intent message = new Intent(BROADCAST_ACTION);
		message.putExtra(SERVICE_CURR_ITEM_FIELD, -1);
		sendBroadcast(message);
	}

	private void makeNotification() {
		// Make the notification
		String notificationText;
		if (!routineFinished) {
			if (mCurrentItem.getmCurrentTime() > 0) {
				notificationText = "Item time: " + RoutineUtils.formatLengthString(RoutineUtils.msecToSec(mCurrentItem.getmCurrentTime()));
			} else {
				notificationText = "Leftover time: " + RoutineUtils.formatLengthString(RoutineUtils.msecToSec(mRoutineClock.getmCarryTime()));
			}
		} else {
			notificationText = "Time is up!";
			mBuilder = null;
		}


		if (mBuilder == null) {
			// Prepare notification button intents
			Intent nextIntent = new Intent(this, ClockService.class);
			nextIntent.setData(mCurrentUri);
			nextIntent.putExtra(SERVICE_COMMAND_FIELD, CLOCK_SERVICE_NEXT_ITEM);
			PendingIntent pNextIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), nextIntent, 0);

			// Prepare notification button intents
			Intent prevIntent = new Intent(this, ClockService.class);
			prevIntent.setData(mCurrentUri);
			prevIntent.putExtra(SERVICE_COMMAND_FIELD, CLOCK_SERVICE_PREV_ITEM);
			PendingIntent pPrevIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), prevIntent, 0);

			mBuilder =
					new Builder(this)

							.setSmallIcon(R.drawable.ic_stat_watch)
//                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
							.setPriority(2)
							.setContentTitle(mCurrentItem.getmItemName())
							.setContentText(notificationText);

			if (!routineFinished) {
				mBuilder.addAction(R.drawable.ic_arrow_left_bold_circle_outline_grey600_36dp, "Prev", pPrevIntent);
				mBuilder.addAction(R.drawable.ic_arrow_right_bold_circle_outline_grey600_36dp, "Next", pNextIntent);

			}
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
		getContentResolver().update(mCurrentUri, values, null, null);

		long updatedRoutineId = mRoutineClock.getmId();
		ArrayList<RoutineItem> itemsList = mRoutineClock.getmItemsList();
		// Update items
		for (int i = 0; i < mRoutineClock.getmRoutineItemsNum(); i++) {
			RoutineItem item = itemsList.get(i);
			Uri updateUri = ContentUris.withAppendedId(RoutineContract.ItemEntry.CONTENT_URI, item.getmId());
			ContentValues itemValues = new ContentValues();
			itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME, (int) item.getmAverageTime());
			itemValues.put(RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME, item.getmElapsedTime());
			itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, item.getmCurrentTime());
			itemValues.put(RoutineContract.ItemEntry.COLUMN_START_TIME, item.getStartTime());

			int rowsAffected = getContentResolver().update(updateUri, itemValues, null, null);
		}
	}

	// Helper methods
	private long calculateEndTime() {
		Date d = new Date(System.currentTimeMillis() + mRoutineClock.getmLength());
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		Log.e(LOG_TAG, "Hour of the day = " + Calendar.HOUR_OF_DAY);
		long endTime = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
		Log.e(LOG_TAG, "Minute of the day = " + Calendar.HOUR_OF_DAY);
		endTime += c.get(Calendar.MINUTE) * 60 * 1000;
		Log.e(LOG_TAG, "Second of the day = " + Calendar.SECOND);
		endTime += c.get(Calendar.SECOND) * 1000;

		return endTime;
	}

	// Writes to shared preferences

	private void updateLengthPref(long length) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(SERVICE_PREFERENCE_LENGTH_WHEN_STARTED, length);
		editor.apply();
	}

	private void updateInterruptPref(long time) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(SERVICE_PREFERENCE_INTERRUPT_TIME, time);
		editor.apply();
	}

	private long readInterruptTime() {
		return mPrefs.getLong(SERVICE_PREFERENCE_INTERRUPT_TIME, 0);
	}

	private void setStartedRoutine() {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(CLOCK_ROUTINE_IN_PROGRESS, mRoutineClock.getmId());
		editor.apply();
	}

	private void setFinishedRoutine() {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(CLOCK_ROUTINE_IN_PROGRESS, -1);
		editor.apply();
	}

	private void updateInterrupt(long time) {
		updateInterruptPref(time);
		mRoutineClock.setmInterruptTime(time);
	}

	// VIBRATIONS

	// Registers a vibration event in the future, with the type in the args
	private void registerVibration(int vibrationType, long timeInFuture) {
		AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(this, VibrationNotificationReceiver.class);
		i.putExtra(VIBRATION_PATTERN, vibrationType);

		PendingIntent pi = PendingIntent.getBroadcast(this, vibrationType, i, PendingIntent.FLAG_UPDATE_CURRENT);
		mgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeInFuture, pi);
	}

	private void cancelVibration(int vibrationType) {
		AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(this, VibrationNotificationReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, vibrationType, i, PendingIntent.FLAG_CANCEL_CURRENT);
		if (mgr != null) {
			mgr.cancel(pi);
		}
	}

	// If there is less than 5 seconds left until the end, the app tries to vibrate on the main thread
	// else it tries to work with a broadcast receiver
	private void registerEndVibration() {
		long endTime = mRoutineClock.getmLength();
		if (endTime < 5500) {
			shouldVibrateInServiceNext = true;
			inServiceVibrationPattern = END_PATTERN;
		} else {
			registerVibration(VIBRATION_END_ALARM, endTime);
		}
	}

	private void cancelEndVibration() {
		cancelVibration(VIBRATION_END_ALARM);
	}

	private void registerItemVibrations() {
		long startTime = mCurrentItem.getmCurrentTime();
		long halfTime = startTime / 2;
		long thirdTime = (long) (startTime * (2.0 / 3));
		long carryZero = startTime + mRoutineClock.getmCarryTime();

		Log.e(LOG_TAG, "Start time in registerItemVibrations: " + startTime);

		// Setting item end vibs
		if (startTime < 5500) {
			shouldVibrateInServiceNext = true;
			inServiceVibrationPattern = LONG_PATTERN;
		} else {
			Log.e(LOG_TAG, "Registering item end vibration.");
			registerVibration(VIBRATION_MAIN_ZERO, startTime);
		}

		// Setting half-time vibs
		if (halfTime < 5500) {
			shouldVibrateInServiceNext = true;
			inServiceVibrationPattern = SHORT_PATTERN;
		} else {
			Log.e(LOG_TAG, "Registering halftime vibration.");
			registerVibration(VIBRATION_HALFTIME_ALARM, halfTime);
		}

		// Settings third vibs
		if (startTime > 30500) {
			if (thirdTime < 5500) {
				shouldVibrateInServiceNext = true;
				inServiceVibrationPattern = SHORT_PATTERN;
			} else {
				Log.e(LOG_TAG, "Registering thirdtime vibration.");
				registerVibration(VIBRATION_THIRD_ALARM, thirdTime);
			}
		}

		// Setting carry end vibs
		if (carryZero < 5500) {
			shouldVibrateInServiceNext = true;
			inServiceVibrationPattern = LONG_PATTERN;
		} else {
			Log.e(LOG_TAG, "Registering carry end vibration.");
			registerVibration(VIBRATION_CARRY_ZERO, carryZero);
		}

	}

	private void cancelItemVibrations() {
		cancelVibration(VIBRATION_CARRY_ZERO);
		cancelVibration(VIBRATION_MAIN_ZERO);
		cancelVibration(VIBRATION_THIRD_ALARM);
		cancelVibration(VIBRATION_HALFTIME_ALARM);

	}

	private PowerManager.WakeLock getWakeLock() {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		PowerManager.WakeLock wl = null;
		if (pm != null) {
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DailyRoutines:ServiceTempWakelock");
		}
		return wl;
	}

	private void vibrateInService(long[] pattern) {
		if (shouldVibrateInServiceNext) {
			Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			if (vibr != null) {
				vibr.vibrate(pattern, -1);
			}
			shouldVibrateInServiceNext = false;
		}
	}


	// Just a helper method encapsulating this common pattern
	private void startCountdownTimer(long millisInFuture, long countdownInterval) {
		mCountdownTimer = new ClockCountdownTimer(millisInFuture, countdownInterval);
		mCountdownTimer.start();
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private void startMyOwnForeground(Builder mBuilder) {
		CharSequence name = "Daily Routines Notification Channel";
		String description = "Daily Routines Notification Channel";
		int importance = NotificationManager.IMPORTANCE_DEFAULT;
		NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
		channel.setDescription(description);
		// Register the channel with the system; you can't change the importance
		// or other notification behaviors after this
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		assert notificationManager != null;
		notificationManager.createNotificationChannel(channel);

		mBuilder.setChannelId(CHANNEL_ID)
				.setOnlyAlertOnce(true);

		Notification notification = mBuilder.setOngoing(true)
				                            .setContentTitle(this.getString(R.string.notification_message))
				                            .setPriority(NotificationManager.IMPORTANCE_MIN)
				                            .setCategory(Notification.CATEGORY_SERVICE)
				                            .build();
		startForeground((int) mRoutineClock.getmId(), notification);
	}

	// On Bind not used
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class ClockCountdownTimer extends CountDownTimer {

		private static final int COUNTER_PERIOD = 10;
		private long preciseEndTimestamp;
		private int counter;
		private long avgDiff;

		/**
		 * @param millisInFuture    The number of millis in the future from the call
		 *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
		 *                          is called.
		 * @param countDownInterval The interval along the way to receive
		 *                          {@link #onTick(long)} callbacks.
		 */
		public ClockCountdownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			preciseEndTimestamp = System.currentTimeMillis() + mRoutineClock.getmLength();
			Log.e(LOG_TAG, "End time: " + new Date(preciseEndTimestamp).toString());

			counter = -1;
		}

		private void balanceDifference(long currentDiff) {
			Log.e(LOG_TAG, "Balance counter: " + counter);
			if (counter == -1) {
				avgDiff = currentDiff;
			} else {
				avgDiff = (avgDiff + currentDiff) / 2;
			}

			if (counter == COUNTER_PERIOD) {
				long newInterrupt = mRoutineClock.getmInterruptTime() + avgDiff;
				updateInterrupt(newInterrupt);

				counter = 0;
			}
			counter++;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			PowerManager.WakeLock wl = getWakeLock();
			wl.acquire(10 * 60 * 1000L /*10 minutes*/);

			long length = 0;
			for (int i = mRoutineClock.getmCurrentItemIndex(); i < mRoutineClock.getmRoutineItemsNum(); i++) {
				length += mRoutineClock.getmItemsList().get(i).getmCurrentTime();
			}

			long endDifference = preciseEndTimestamp - (System.currentTimeMillis() + length + mRoutineClock.getmCarryTime());
			Log.e(LOG_TAG, "Remaining time ends on: " + new Date(System.currentTimeMillis() + length + mRoutineClock.getmCarryTime()));
			Log.e(LOG_TAG, "----------- End difference in ms: " + endDifference + "------------------");

			if (previouslyVibrated && justVibrated) {
				justVibrated = false;
				previouslyVibrated = false;
			}
			if (!previouslyVibrated && justVibrated) {
				previouslyVibrated = true;
			}

			long diffTime = 0;

			if (mScreenIsOn) {
				balanceDifference(endDifference);
				long currentItemTime = mCurrentItem.getmCurrentTime();
				if (currentItemTime <= (mCurrentItem.getStartTime() / 2) + 500
						    && currentItemTime > (mCurrentItem.getStartTime() / 2) - 500
						    && currentItemTime != 0 && sVibrateOn
						    && !justVibrated) {
					vibrateInService(inServiceVibrationPattern);
					justVibrated = true;
				} else if (currentItemTime <= (mCurrentItem.getStartTime() / 3) + 500
						           && currentItemTime > (mCurrentItem.getStartTime() / 3) - 500
						           && currentItemTime != 0 && sVibrateOn
						           && !justVibrated) {
					vibrateInService(inServiceVibrationPattern);
					justVibrated = true;
				}

				if (currentItemTime > 0) {
					if (currentItemTime <= 1500
							    && currentItemTime > 500
							    && sVibrateOn
							    && !justVibrated) {
						vibrateInService(inServiceVibrationPattern);
						justVibrated = true;
					}
					// Subtract one second or zero the counter
					if (currentItemTime - 1000 <= 0) {
						mCurrentItem.setmCurrentTime(0);
					} else {
						diffTime = System.currentTimeMillis() - mRoutineClock.getmInterruptTime();
						mCurrentItem.setmCurrentTime(currentItemTime - diffTime);
						Log.e(LOG_TAG, "Current interval: " + diffTime);
						updateInterrupt(System.currentTimeMillis());
					}
				} else {
					long carry = mRoutineClock.getmCarryTime();
					if (carry <= 1500 && carry > 500 && sVibrateOn && !justVibrated) {
						vibrateInService(inServiceVibrationPattern);
						justVibrated = true;
					}
					diffTime = System.currentTimeMillis() - mRoutineClock.getmInterruptTime();
					mRoutineClock.setmCarryTime(carry - diffTime);
					Log.e(LOG_TAG, "Current interval: " + diffTime);
					updateInterrupt(System.currentTimeMillis());
				}

				// Check if routine ended. Interrupt if timer would continue with no time left.
				if ((mRoutineClock.getmCurrentItemIndex() + 1 == mRoutineClock.getmRoutineItemsNum()
						     && mRoutineClock.getmCarryTime() <= 0
						     && mCurrentItem.getmCurrentTime() == 0)
						    ||
						    (System.currentTimeMillis() >= preciseEndTimestamp
								     && mRoutineClock.ismEndTimeRequired())) {
					onFinish();
				}

				// Send broadcast intent to Activity
				sendMessage();
				makeNotification();
				writeRoutineToDB();
				// Update clocks
				mCurrentItem.incrementElapsedTime(diffTime);
			}
			wl.release();
		}

		@Override
		public void onFinish() {

			mCurrentItem.incrementElapsedTime(1000);

			// When the internal countdown reaches zero, but the visual counter still has
			// some time left, restart the countdown with that much time
			// This is needed because there is some unpunctual behaviour that's still not found
			// However, this checks if it's not advancing the user set limit. If it would step
			// over, it kills the countdown
			if (mRoutineClock.ismEndTimeRequired()) {
				if (mRoutineClock.getmCarryTime() > 1000
						    && RoutineUtils.getCurrentTimeInSec() < mRoutineClock.getmEndTime() / 1000) {
					startCountdownTimer(mRoutineClock.getmCarryTime(), COUNTDOWN_INTERVAL_CONST);
					return;
				}
			} else if (mRoutineClock.getmCarryTime() > 1000) {
				startCountdownTimer(mRoutineClock.getmCarryTime(), COUNTDOWN_INTERVAL_CONST);
				return;
			}

			routineFinished = true;
			makeNotification();
			// Stop countdown
			if (sVibrateOn) {
				vibrateInService(inServiceVibrationPattern);
			}

			mRepeatEndMessageTimer = new Timer();
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					if (shouldSpeak) {
						sendMessage(-1);
					} else {
						if (mRepeatEndMessageTimer != null) {
							mRepeatEndMessageTimer.cancel();
							mRepeatEndMessageTimer = null;
						}
					}

				}
			};
			mRepeatEndMessageTimer.schedule(timerTask, 1000, 1000);
		}
	}
}

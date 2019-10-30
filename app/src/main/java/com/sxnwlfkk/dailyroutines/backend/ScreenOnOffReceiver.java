package com.sxnwlfkk.dailyroutines.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sxnwlfkk.dailyroutines.views.clock.ClockService;

import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_SCREEN_OFF;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.CLOCK_SERVICE_SCREEN_ON;
import static com.sxnwlfkk.dailyroutines.views.clock.ClockService.SERVICE_COMMAND_FIELD;

/**
 * Created by sxnwlfkk on 2017.06.19..
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean screenOff = false;

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

			screenOff = true;

		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

			screenOff = false;

		}

		Intent newIntent = new Intent(context, ClockService.class);
		if (screenOff) {
			newIntent.putExtra(SERVICE_COMMAND_FIELD, CLOCK_SERVICE_SCREEN_OFF);
		} else {
			newIntent.putExtra(SERVICE_COMMAND_FIELD, CLOCK_SERVICE_SCREEN_ON);
		}
		context.startService(newIntent);
	}
}

package com.sxnwlfkk.dailyroutines.views.clock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by cs on 2017.04.20..
 */

public class ClockActivityNew extends Activity {

    // VARS

    // Button click listeners


    // On create
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create or bind service
    }

    // On destroy
    @Override
    protected void onDestroy() {
        // Unbind service

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register receiver
    }

    @Override
    protected void onPause() {
        // Unregister receiver
        super.onPause();

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get data from intent
            // Refresh the screen
        }
    };
}

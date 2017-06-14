package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.BuildConfig;
import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.AlarmNotificationReceiver;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.clock.ClockActivity;
import com.sxnwlfkk.dailyroutines.views.editActivity.EditActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS
    // Log tag
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PREFERNCES_APP_VERSION = "app_version";

    private ActionBar mActionBar;
    private ProgressBar mProgressBar;
    TextView mEmptyStateTextView;

    // ID of background loader
    private static final int ROUTINE_LOADER = 20;
    // Cursor adapter
    MainRoutineCursorAdapter mainRoutineCursorAdapter;

    ListView mRoutineListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Mandatory
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for app version, and update
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int version = preferences.getInt(PREFERNCES_APP_VERSION, -1);
        if (version < BuildConfig.VERSION_CODE) {
            preferences.edit()
                    .putInt(PREFERNCES_APP_VERSION, BuildConfig.VERSION_CODE)
                    .putBoolean(AlarmNotificationReceiver.ALARM_SETUP_WAS_DONE, false)
                    .apply();
        }


        // Setting up the toolbar
        mActionBar = getActionBar();
        mActionBar.setTitle(R.string.main_title);

        // Setting up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_ritual);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete this line, when tests here are obsolete
//                TestDBProvider test = new TestDBProvider();

                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        // Setting up main list view
        mRoutineListView = (ListView) findViewById(R.id.main_list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mRoutineListView.setEmptyView(mEmptyStateTextView);

        // Initialize cursor adapter
        mainRoutineCursorAdapter = new MainRoutineCursorAdapter(this, null);
        mRoutineListView.setAdapter(mainRoutineCursorAdapter);

        // List item click listener
        mRoutineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);

                Uri currentUri = ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, id);
                intent.setData(currentUri);

                startActivity(intent);
            }
        });
        // Quickly get to edit routine
        mRoutineListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                Uri currentUri = ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, id);
                intent.setData(currentUri);

                startActivity(intent);
                return false;
            }
        });

        // Check if alarms were set up, then
        boolean alarmsWereSetUp = preferences.getBoolean(AlarmNotificationReceiver.ALARM_SETUP_WAS_DONE, false);
        Log.e(LOG_TAG, "alarmsWereSetUp = " + Boolean.toString(alarmsWereSetUp));
        if (!alarmsWereSetUp) AlarmNotificationReceiver.scheduleAlarms(this);

        getLoaderManager().initLoader(ROUTINE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_preferences_button:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void purgeDatabase() {
        getContentResolver().delete(RoutineContract.RoutineEntry.CONTENT_URI, null, null);
        getContentResolver().delete(RoutineContract.ItemEntry.CONTENT_URI, null, null);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                RoutineContract.RoutineEntry._ID,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
                RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME
        };

        return new CursorLoader(this,
                RoutineContract.RoutineEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                int itemStarted = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
                Log.e(LOG_TAG, "Main onloadFinished. Routine's current item is: " + itemStarted);
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                if (itemStarted > -1) {
                    Intent intent = new Intent(this, ClockActivity.class);
                    intent.setData(ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, id));
                    startActivity(intent);
                }
            } while (cursor.moveToNext());
        }
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        if (cursor.getCount() == 0) {
            mEmptyStateTextView.setText(R.string.main_empty_view_text);
        }
        mainRoutineCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mainRoutineCursorAdapter.swapCursor(null);
    }
}


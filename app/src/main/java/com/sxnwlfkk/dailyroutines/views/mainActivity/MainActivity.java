package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sxnwlfkk.dailyroutines.BuildConfig;
import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.AlarmNotificationReceiver;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.data.RoutineDbHelper;
import com.sxnwlfkk.dailyroutines.util.CompositionUtils;
import com.sxnwlfkk.dailyroutines.views.clock.ClockActivity;
import com.sxnwlfkk.dailyroutines.views.editActivity.EditActivity;
import com.sxnwlfkk.dailyroutines.views.guide.GuideActivity;
import com.sxnwlfkk.dailyroutines.views.preference.SettingsActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static com.sxnwlfkk.dailyroutines.util.RoutineUtils.readCurrentRoutine;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS
    // Log tag
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PREFERNCES_APP_VERSION = "app_version";
    public static final String PACKAGE_NAME = "com.sxnwlfkk.dailyroutines";

    private ActionBar mActionBar;
    private ProgressBar mProgressBar;
    private TextView mEmptyStateTextView;
    private Button mGotoButton;
    private long mId;

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
        mGotoButton = (Button) findViewById(R.id.main_menu_goto_button);
        mGotoButton.setVisibility(View.GONE);

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

        // Check if alarms were set up, then
        boolean alarmsWereSetUp = preferences.getBoolean(AlarmNotificationReceiver.ALARM_SETUP_WAS_DONE, false);
        Log.e(LOG_TAG, "alarmsWereSetUp = " + Boolean.toString(alarmsWereSetUp));
        if (!alarmsWereSetUp) AlarmNotificationReceiver.scheduleAlarms(this);

        CompositionUtils.updateDatabase(this.getBaseContext());

        getLoaderManager().initLoader(ROUTINE_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        long currentRoutine = readCurrentRoutine(this);
        if (currentRoutine > -1) {
            mGotoButton.setVisibility(View.VISIBLE);

            // Get name
            String[] projection = {
                    RoutineContract.RoutineEntry._ID,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
            };

            Uri currUri = ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, currentRoutine);

            Cursor cursor = getContentResolver().query(
                    currUri,
                    projection,
                    null,
                    null,
                    null);

            if (cursor != null) {
                cursor.moveToFirst();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                mGotoButton.setText("Continue routine in progress \n(" + name + ")");
            } else {
                mGotoButton.setText("Continue routine in progress");
            }

            mGotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), ClockActivity.class);
                            long currentRoutine = readCurrentRoutine(getApplicationContext());
                            i.setData(ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, currentRoutine));
                            startActivity(i);
                }
            });
        } else {
            mGotoButton.setVisibility(View.GONE);
        }
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
            case R.id.menu_main_backup_button:
                DialogInterface.OnClickListener backupButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Backup" button
                                if (isStoragePermissionGranted()) {
                                    backupRoutines();
                                    Toast.makeText(getApplicationContext(), "Your routines are backed up.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                showBackupDialog(backupButtonClickListener);
                break;
            case R.id.menu_main_restore_button:
                DialogInterface.OnClickListener restoreButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Restore" button
                                if (isStoragePermissionGranted()) {
                                    AlarmNotificationReceiver.cancelAlarms(getApplicationContext());
                                    restoreRoutines();
                                    AlarmNotificationReceiver.scheduleAlarms(getApplicationContext());
                                    Toast.makeText(getApplicationContext(), "Your routines have been restored.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                showRestoreDialog(restoreButtonClickListener);
                break;
            case R.id.menu_main_guide_button:
                Intent i = new Intent(this, GuideActivity.class);
                startActivity(i);
                break;
        }
        return false;
    }

    private void showRestoreDialog(DialogInterface.OnClickListener restoreButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.restore_dialog_msg);
        builder.setPositiveButton(R.string.yes, restoreButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Nope" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void restoreRoutines() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            Log.e(LOG_TAG, "Can write SD: " + sd.canWrite());

            if (sd.canWrite()) {
                Log.e(LOG_TAG, "Writing data.");
                String currentDBPath = "//data//"+getPackageName()+"//databases//"+RoutineDbHelper.DATABASE_NAME+"";
                String backupDBPath = "/daily_routines_backup.drdb";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    Log.e(LOG_TAG, "DB exists, overwriting.");
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG,"Permission is granted");
                return true;
            } else {

                Log.e(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.e(LOG_TAG,"Permission is granted");
            return true;
        }
    }

    private void showBackupDialog(DialogInterface.OnClickListener backupButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.backup_dialog_msg);
        builder.setPositiveButton(R.string.yes, backupButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Nope" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.e(LOG_TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks reseding this permission
            Toast.makeText(this, "Permission granted, please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void backupRoutines() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            Log.e(LOG_TAG, "Can write SD: " + sd.canWrite());

            if (sd.canWrite()) {
                Log.e(LOG_TAG, "Writing data.");
                String currentDBPath = "//data//"+getPackageName()+"//databases//"+RoutineDbHelper.DATABASE_NAME+"";
                String backupDBPath = "/daily_routines_backup.drdb";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    Log.e(LOG_TAG, "DB exists, writing.");
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG,
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
//            cursor.moveToFirst();
//            do {
//                int itemStarted = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
//                Log.e(LOG_TAG, "Main onloadFinished. Routine's current item is: " + itemStarted);
//                mId = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
//                if (itemStarted > -1) {
//                    mGotoButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent i = new Intent(getApplicationContext(), ClockActivity.class);
//                            i.setData(ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, mId));
//                            startActivity(i);
//                        }
//                    });
//                    mGotoButton.setVisibility(View.VISIBLE);
//                }
//            } while (cursor.moveToNext());
//        }
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
        if (cursor.getCount() == 0) {
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
            mEmptyStateTextView.setText(R.string.main_empty_view_text);
        }
        mainRoutineCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mainRoutineCursorAdapter.swapCursor(null);
    }
}


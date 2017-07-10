package com.sxnwlfkk.dailyroutines.views.profileActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.AlarmNotificationReceiver;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineCloner;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.clock.ClockActivity;
import com.sxnwlfkk.dailyroutines.views.editActivity.EditActivity;
import com.sxnwlfkk.dailyroutines.views.mainActivity.MainActivity;

public class ProfileActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS
    public static final String LOG_TAG = ProfileActivity.class.getSimpleName();

    private static final int PROFILE_ROUTINE_LOADER = 21;
    private static final int PROFILE_ITEMS_LOADER = 22;

    // Uri of the item
    private Uri mCurrentUri;

    // TextFields
    private TextView mRoutineName;
    private TextView mRoutineLength;
    private TextView mRoutineItemNum;
    private TextView mAvgRoutineLength;

    private ProfileCursorAdapter mCursorAdapter;
    private long mAvgTime;
    private long mLength;
    private long mRoutineId;

    // Button click listener
    private View.OnClickListener mStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ProfileActivity.this, ClockActivity.class);
            intent.setData(mCurrentUri);
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Getting intent
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // Action Bar
        getActionBar().setHomeButtonEnabled(true);

        // Hook up start button
        Button startButton = (Button) findViewById(R.id.profile_start_button);
        startButton.setOnClickListener(mStartButtonClickListener);

        // Finding the Routine Text fields
        mRoutineLength = (TextView) findViewById(R.id.profile_routine_length);
        mRoutineItemNum = (TextView) findViewById(R.id.profile_item_number);
        mAvgRoutineLength = (TextView) findViewById(R.id.profile_routine_avg_length);

        ListView listView = (ListView) findViewById(R.id.profile_list_view);
        mCursorAdapter = new ProfileCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        getLoaderManager().initLoader(PROFILE_ROUTINE_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_ITEMS_LOADER, null, this);

        // Initialize variables
        mLength = -1;
        mAvgTime = -1;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Button startButton = (Button) findViewById(R.id.profile_start_button);
        long currentRoutine = RoutineUtils.readCurrentRoutine(this);
        if (currentRoutine == mRoutineId) {
            startButton.setText("Continue routine");
            startButton.setOnClickListener(mStartButtonClickListener);
        } else if (currentRoutine == -1) {
            startButton.setText("Start routine");
            startButton.setOnClickListener(mStartButtonClickListener);
        } else {
            startButton.setText("Another routine in progress");
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    return;
                }
            });
        }

    }

    // Dialog
    private void showDeleteRoutineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_routine);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Sure, I want to delete" button, so delete the item and
                // dismiss the dialog
                if (dialog != null) {
                    deleteRoutine();
                    AlarmNotificationReceiver.cancelAlarm(getApplicationContext(), mCurrentUri);
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showResetStatisticsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.reset_statistics_dialog);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    resetStatistics();
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Back Button
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        long currentRoutine = RoutineUtils.readCurrentRoutine(this);
        switch (item.getItemId()) {
            case R.id.menu_profile_edit_routine:
                if (currentRoutine != mRoutineId) {
                    Intent intent = new Intent(ProfileActivity.this, EditActivity.class);
                    intent.setData(mCurrentUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Can't modify running routine. Please stop it first and try again.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_profile_delete_routine:
                if (currentRoutine != mRoutineId) {
                    showDeleteRoutineDialog();
                } else {
                    Toast.makeText(this, "Can't delete running routine. Please stop it first and try again.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_profile_reset_statistics:
                if (currentRoutine != mRoutineId) {
                    showResetStatisticsDialog();
                } else {
                    Toast.makeText(this, "Can't modify running routine. Please stop it first and try again.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_profile_clone_routine:
                cloneCurrentRoutine();
                break;
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void cloneCurrentRoutine() {
        Toast.makeText(getApplicationContext(), R.string.clone_routine_started_cloning, Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RoutineCloner cloner = new RoutineCloner();
                long response = cloner.cloneRoutine(getApplicationContext(), mCurrentUri);
                if (response != 0) {
                    // Success
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.clone_routine_success_message, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Failure
                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.clone_routine_failure_message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void resetStatistics() {
        ContentValues values = new ContentValues();
        values.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME, 0);

        String projection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
        String[] projArgs = new String[] { String.valueOf(ContentUris.parseId(mCurrentUri)) };

        getContentResolver().update(
                RoutineContract.ItemEntry.CONTENT_URI,
                values,
                projection,
                projArgs
        );

        // Restart activity
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void deleteRoutine() {
        // Delete routine
        getContentResolver().delete(mCurrentUri, null, null);
        // Delete all items with the parent routines number
        String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
        String[] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(mCurrentUri)) };
        getContentResolver().delete(RoutineContract.ItemEntry.CONTENT_URI, selection, selectionArgs);
        NavUtils.navigateUpFromSameTask(ProfileActivity.this);
    }

    // Loader
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Log.d(LOG_TAG, "In onCreateLoader");

        long id = 0;
        try {
            id = ContentUris.parseId(mCurrentUri);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid argument in URI (URI is not ending with .../#): " + mCurrentUri);
            return null;
        }

        if (loaderId == PROFILE_ROUTINE_LOADER) {

            String[] projection = {
                    RoutineContract.RoutineEntry._ID,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
                    RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG,
            };

            return new CursorLoader(this,
                    mCurrentUri,
                    projection,
                    null,
                    null,
                    null);

        } else if (loaderId == PROFILE_ITEMS_LOADER) {
            String[] projection = new String[] {
                    RoutineContract.ItemEntry._ID,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
                    RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
                    RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NO,
            };

            String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
            String[] selectionArgs = new String[] { String.valueOf(id) };

            return new CursorLoader(this,
                    RoutineContract.ItemEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        if (cursor.getCount() < 1) return;
        cursor.moveToFirst();

        switch (loaderId) {
            case PROFILE_ROUTINE_LOADER:
                cursor.moveToFirst();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                mLength = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
                int itemNum = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));
                int endTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
                boolean requireEnd = (cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END)) == 1);
                mRoutineId = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                String rrule = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG));

                // Check if routine started when coming form notification
                int itemStarted = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));

                mRoutineLength.setText(RoutineUtils.formatLengthString(
                        RoutineUtils.msecToSec(mLength)));
                mRoutineItemNum.setText(String.valueOf(itemNum));

                if (requireEnd) {
                    TextView numOfItems = (TextView) findViewById(R.id.profile_num_of_items_text);
                    numOfItems.setText(R.string.optimal_start_text);
                    Spanned startTimeText = RoutineUtils.getOptimalStartText(endTime, mLength, rrule);
                    mRoutineItemNum.setText(startTimeText);
                } else {
                    TextView numOfItems = (TextView) findViewById(R.id.profile_num_of_items_text);
                    numOfItems.setText(R.string.number_of_items_text);
                    mRoutineItemNum.setText(mRoutineItemNum.getText());
                }

                ActionBar aBar = getActionBar();
                aBar.setTitle(name);

                break;
            case PROFILE_ITEMS_LOADER:
                calculateAvgTime(cursor);
                mCursorAdapter.swapCursor(cursor);
                break;
        }


        // Show avg time if both loaders are in
        if (mLength != -1 && mAvgTime != -1) {
            mAvgRoutineLength.setVisibility(View.VISIBLE);
            mAvgRoutineLength.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(mAvgTime)));
            mAvgRoutineLength.setTextColor(getResources().getColor(R.color.material_indigo_lighten1));
        } else {
            mAvgRoutineLength.setVisibility(View.INVISIBLE);
        }

    }

    private void calculateAvgTime(Cursor cursor) {
        long avg = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int itemAvg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));
                avg += itemAvg;
                cursor.moveToNext();
            }
            cursor.moveToFirst();
        }
        if (avg != 0) mAvgTime = avg;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();

        switch (loaderId) {
            case PROFILE_ROUTINE_LOADER:
                mRoutineItemNum.setText("");
                mRoutineLength.setText("");
                break;
            case PROFILE_ITEMS_LOADER:
                mCursorAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }
}

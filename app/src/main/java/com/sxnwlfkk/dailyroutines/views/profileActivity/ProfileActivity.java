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
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.AlarmNotificationReceiver;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
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

    private ProfileCursorAdapter mCursorAdapter;

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

        ListView listView = (ListView) findViewById(R.id.profile_list_view);
        mCursorAdapter = new ProfileCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        getLoaderManager().initLoader(PROFILE_ROUTINE_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_ITEMS_LOADER, null, this);
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
        switch (item.getItemId()) {
            case R.id.menu_profile_edit_routine:
                Intent intent = new Intent(ProfileActivity.this, EditActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
                break;
            case R.id.menu_profile_delete_routine:
                showDeleteRoutineDialog();
                break;
            case R.id.menu_profile_reset_statistics:
                showResetStatisticsDialog();
                break;
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
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
                int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
                int itemNum = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));
                int endTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
                boolean requireEnd = (cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END)) == 1);

                // Check if routine started when coming form notification
                int itemStarted = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                if (itemStarted > -1) {
                    Intent intent = new Intent(this, ClockActivity.class);
                    intent.setData(ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, id));
                    startActivity(intent);
                }

                mRoutineLength.setText(RoutineUtils.formatLengthString(length));
                mRoutineItemNum.setText(String.valueOf(itemNum));

                if (requireEnd) {
                    TextView numOfItems = (TextView) findViewById(R.id.profile_num_of_items_text);
                    numOfItems.setText(R.string.optimal_start_text);
                    mRoutineItemNum.setText(RoutineUtils.formatClockTimeString(RoutineUtils.calculateIdealStartTime(endTime, length)));
                }

                ActionBar aBar = getActionBar();
                aBar.setTitle(name);

                break;
            case PROFILE_ITEMS_LOADER:
                mCursorAdapter.swapCursor(cursor);
                break;
        }

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

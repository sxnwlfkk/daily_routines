package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.clock.ClockActivity;
import com.sxnwlfkk.dailyroutines.views.editActivity.EditActivity;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS
    // Log tag
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ActionBar mActionBar;

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
        TextView mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mEmptyStateTextView.setText(R.string.main_empty_view_text);
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
            case R.id.main_menu_delete_all:
                purgeDatabase();
        }

        return super.onOptionsItemSelected(item);
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
        mainRoutineCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mainRoutineCursorAdapter.swapCursor(null);
    }

    // Test class for interactions with DB provider
    // Also it's setting up items in DB to work with
    private class TestDBProvider {

        Uri mRoutinetUri;
        Uri mItemUri;

        public TestDBProvider() {
            run();
        }

        private void run () {
            // Routines
            insertRoutine();
            queryRoutine();
            updateRoutine();
            deleteRoutine();

            // Items
            insertItem();
            queryItem();
            updateItem();
            deleteItem();
        }

        private void insertRoutine() {
            ContentValues values = new ContentValues();
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, "Test routine");
            values.put(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM, -1);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY, 0);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, 10);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, 300);

            mRoutinetUri = getContentResolver().insert(RoutineContract.RoutineEntry.CONTENT_URI, values);
            Log.d(LOG_TAG, "Insert routine return uri: " + mRoutinetUri);
        }

        private void queryRoutine() {
            String[] projection = {
                    RoutineContract.RoutineEntry._ID,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
            };

            Cursor cursor = getContentResolver().query(RoutineContract.RoutineEntry.CONTENT_URI,
                    projection, null, null, null);

            Log.d(LOG_TAG, "Returned rows from routine query: " + cursor.getCount());

            // Query with id
            cursor = getContentResolver().query(mRoutinetUri, null, null, null, null, null);
            Log.d(LOG_TAG, "Returned rows from ID routine query (should be 1): " + cursor.getCount());
        }

        private void updateRoutine() {
            ContentValues values = new ContentValues();
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, "Test updated");
            values.put(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM, 1);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY, 20);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, 11);
            values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, 400);

            long rowsAffected = getContentResolver().update(mRoutinetUri, values, null, null);
            Log.d(LOG_TAG, "Returned rows from ID routine update (should be 1): " + rowsAffected);
        }

        private void deleteRoutine() {
            int rowsAffected = getContentResolver().delete(mRoutinetUri, null, null);
            Log.d(LOG_TAG, "Returned rows from ID routine delete (should be 1): " + rowsAffected);
        }

        private void insertItem() {
            insertRoutine();
            long routId = ContentUris.parseId(mRoutinetUri);

            ContentValues values = new ContentValues();
            values.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, routId);
            values.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, "Test item");
            values.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, 30);
            values.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, 30);
            values.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME, 30);
            values.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, 30);

            for (int i = 0; i < 5; i++) {
                getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, values);
            }

            mItemUri = getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, values);
            Log.d(LOG_TAG, "Insert item return uri: " + mItemUri);
        }

        private void queryItem() {
            Cursor cursor;
            long routId = ContentUris.parseId(mRoutinetUri);

            String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
            String[] selectionArgs = new String[] { String.valueOf(routId) };

            cursor = getContentResolver().query(RoutineContract.ItemEntry.CONTENT_URI, null, selection, selectionArgs, null);

            Log.d(LOG_TAG, "Returned rows from items query (should be 6): " + cursor.getCount());
        }

        private void updateItem() {
            ContentValues values = new ContentValues();
            values.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, "Test item updated");
            long rowsAffected = getContentResolver().update(mItemUri, values, null, null);
            Log.d(LOG_TAG, "Returned rows from ID item update (should be 1): " + rowsAffected);
        }

        private void deleteItem() {
            int rowsAffected = getContentResolver().delete(mItemUri, null, null);
            Log.d(LOG_TAG, "Returned rows from ID item delete (should be 1): " + rowsAffected);

            long routId = ContentUris.parseId(mRoutinetUri);
            String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
            String[] selectionArgs = new String[] { String.valueOf(routId) };

//            rowsAffected = getContentResolver().delete(RoutineContract.ItemEntry.CONTENT_URI, selection, selectionArgs);
//            Log.d(LOG_TAG, "Returned rows from deleting all items of a routine (should be 5): " + rowsAffected);
        }


    }
}


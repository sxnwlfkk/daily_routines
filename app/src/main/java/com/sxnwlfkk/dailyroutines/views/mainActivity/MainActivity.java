package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;

public class MainActivity extends Activity {

    // VARS
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    ListView mRoutineListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRoutineListView = (ListView) findViewById(R.id.main_list);
        TextView mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mRoutineListView.setEmptyView(mEmptyStateTextView);


        /* Set up FAB */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_ritual);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestDBProvider test = new TestDBProvider();
            }
        });


    }

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

            rowsAffected = getContentResolver().delete(RoutineContract.ItemEntry.CONTENT_URI, selection, selectionArgs);
            Log.d(LOG_TAG, "Returned rows from deleting all items of a routine (should be 5): " + rowsAffected);
        }


    }
}


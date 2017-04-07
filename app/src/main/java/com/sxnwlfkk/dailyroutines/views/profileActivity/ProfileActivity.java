package com.sxnwlfkk.dailyroutines.views.profileActivity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        mRoutineName = (TextView) findViewById(R.id.profile_routine_name);
        mRoutineLength = (TextView) findViewById(R.id.profile_routine_length);
        mRoutineItemNum = (TextView) findViewById(R.id.profile_item_number);

        ListView listView = (ListView) findViewById(R.id.profile_list_view);
        mCursorAdapter = new ProfileCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        Log.d(LOG_TAG, "In onCreate before");
        getLoaderManager().initLoader(PROFILE_ROUTINE_LOADER, null, this);
        getLoaderManager().initLoader(PROFILE_ITEMS_LOADER, null, this);
    }

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
//                    RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
            };

            String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
            String[] selectionArgs = new String[] { String.valueOf(id) };

            return new CursorLoader(this,
                    RoutineContract.ItemEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        cursor.moveToFirst();

        switch (loaderId) {
            case PROFILE_ROUTINE_LOADER:
                cursor.moveToFirst();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
                int itemNum = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));

                mRoutineName.setText(name);
                mRoutineItemNum.setText(String.valueOf(length));
                mRoutineLength.setText(String.valueOf(itemNum));

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
                mRoutineName.setText("");
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

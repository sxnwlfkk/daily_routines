package com.sxnwlfkk.dailyroutines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by cs on 2017.04.05..
 */

public class RoutineProvider extends ContentProvider {

    // VARS
    public static final String LOG_TAG = RoutineProvider.class.getSimpleName();

    // URI matcher setup

    private static final int ROUTINES = 100;
    private static final int ROUTINE_ID = 101;
    private static final int ITEMS_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ROUTINES, ROUTINES);
        sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ROUTINES + "/#", ROUTINE_ID);
        sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ITEMS + "/#", ITEMS_ID);
    }

    // DB helper
    private RoutineDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new RoutineDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTINES:
                cursor = db.query(RoutineContract.RoutineEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case ROUTINE_ID:
                selection = RoutineContract.RoutineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(RoutineContract.RoutineEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEMS_ID:
                selection = RoutineContract.ItemTable.COLUMN_PARENT_ROUTINE + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(RoutineContract.ItemTable.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI.");
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}

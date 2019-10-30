package com.sxnwlfkk.dailyroutines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by cs on 2017.04.05..
 */

public class RoutineProvider extends ContentProvider {

	// VARS
	public static final String LOG_TAG = RoutineProvider.class.getSimpleName();

	// URI matcher setup

	private static final int ROUTINES = 100;
	private static final int ROUTINE_ID = 101;
	private static final int ITEMS = 200;
	private static final int ITEM_ID = 201;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ROUTINES, ROUTINES);
		sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ROUTINES + "/#", ROUTINE_ID);
		sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ITEMS, ITEMS);
		sUriMatcher.addURI(RoutineContract.CONTENT_AUTHORITY, RoutineContract.PATH_ITEMS + "/#", ITEM_ID);
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
				// Returns all matches from Routines table
				cursor = db.query(RoutineContract.RoutineEntry.TABLE_NAME, projection,
						selection, selectionArgs, null, null, sortOrder);
				break;
			case ROUTINE_ID:
				// Returns one routine, selected from ID
				selection = RoutineContract.RoutineEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				cursor = db.query(RoutineContract.RoutineEntry.TABLE_NAME,
						projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case ITEMS:
				// Returns all items that have the routine_id specified in the URI
				// I think there is no need for this, simply select the routine_id
				// in the selection
//                selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
//                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
				cursor = db.query(RoutineContract.ItemEntry.TABLE_NAME, projection,
						selection, selectionArgs, null, null, sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI.");
		}

		// I don't know it this'll work, should check it out
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case ROUTINES:
				// Insert new routine in the Routines table
				return insertRoutine(RoutineContract.RoutineEntry.TABLE_NAME, uri, values);
			case ITEMS:
				// Insert new routine in the Items table
				return insertRoutine(RoutineContract.ItemEntry.TABLE_NAME, uri, values);
			default:
				throw new IllegalArgumentException("Insertion is not supported for " + uri);
		}
	}

	private Uri insertRoutine(String tableName, Uri uri, ContentValues values) {
		// TODO: input checking

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		long newRowId = db.insert(tableName, null, values);

		// Once we know the ID of the new row in the table,
		// return the new URI with the ID appended to the end of it

		if (newRowId == -1) {
			Log.e(LOG_TAG, "failed to insert row for " + uri);
		}

		// Notify all observers that his URI has changed
		// Null takes care on Android the observer, it will notify CursorLoader
		getContext().getContentResolver().notifyChange(uri, null);

		return ContentUris.withAppendedId(uri, newRowId);
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case ROUTINES:
				// Updates all matches on Routines table
				return updateRoutine(RoutineContract.RoutineEntry.TABLE_NAME, uri, values, selection, selectionArgs);
			case ROUTINE_ID:
				// Uptades one routine
				selection = RoutineContract.RoutineEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateRoutine(RoutineContract.RoutineEntry.TABLE_NAME, uri, values, selection, selectionArgs);
			case ITEMS:
				// Updates all items based on selection
				// The only use case for this I could think of is to zero out all statictical fields
				return updateRoutine(RoutineContract.ItemEntry.TABLE_NAME, uri, values, selection, selectionArgs);
			case ITEM_ID:
				// Udpates one item based of ID
				selection = RoutineContract.ItemEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				return updateRoutine(RoutineContract.ItemEntry.TABLE_NAME, uri, values, selection, selectionArgs);
			default:
				throw new IllegalArgumentException("Cannot query unknown URI.");
		}
	}

	private int updateRoutine(String tableName, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO: sanity check

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int rowsAffected = db.update(tableName, values, selection, selectionArgs);
		if (rowsAffected != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsAffected;
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int rowsDeleted;

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case ROUTINES:
				// Deletes all routines based on selection

				rowsDeleted = db.delete(RoutineContract.RoutineEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case ROUTINE_ID:
				// Deletes one routines based on ID from URI

				selection = RoutineContract.RoutineEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				rowsDeleted = db.delete(RoutineContract.RoutineEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case ITEMS:
				// Deletes all items based on selection
				// This should be used when deleting a routine

				rowsDeleted = db.delete(RoutineContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case ITEM_ID:
				// Deletes one item based on ID from URI

				selection = RoutineContract.ItemEntry._ID + "=?";
				selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
				rowsDeleted = db.delete(RoutineContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Deletion is not supported for " + uri);
		}

		if (rowsDeleted != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsDeleted;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}
}

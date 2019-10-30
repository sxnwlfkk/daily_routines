package com.sxnwlfkk.dailyroutines.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sxnwlfkk.dailyroutines.classes.RoutineItem;

import java.util.ArrayList;

/**
 * Created by sxnwlfkk on 2017.06.21..
 */

public class RoutineCloner {

	private long mId;
	private Uri mUri;
	private long rId;
	private String rName;
	private int rEndTime;
	private boolean rEndTimeReq;
	private int rCarryTime;
	private int rCurrItem;
	private int rItemsNumber;
	private int rTimesUsed;
	private int rInterruptTime;
	private long rRoutineLength;
	private ArrayList<RoutineItem> mRoutineItems;
	private String rRule;

	public long cloneRoutine(Context context, Uri uri) {

		mUri = uri;
		mId = ContentUris.parseId(mUri);

		loadRoutineData(context);
		loadItemsData(context);

		long newId = saveAsNewRoutine(context);

		return newId;
	}

	private void loadRoutineData(Context context) {

		String[] projection = {
				RoutineContract.RoutineEntry._ID,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY,
				RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_INTERRUPT_TIME,
				RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG,
		};

		Cursor cursor = context.getContentResolver().query(mUri, projection, null, null, null);
		cursor.moveToFirst();

		rId = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
		rName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
		rEndTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
		rEndTimeReq = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END)) == 1;
		rCarryTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY));
		rItemsNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER));
		rTimesUsed = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED));
		rRoutineLength = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
		rRule = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG));
	}

	private void loadItemsData(Context context) {
		String[] projectionItems = new String[]{
				RoutineContract.ItemEntry._ID,
				RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
				RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
				RoutineContract.ItemEntry.COLUMN_ITEM_NO,
				RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
		};

		String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
		String[] selectionArgs = new String[]{String.valueOf(mId)};
		Cursor cursor = context.getContentResolver().query(RoutineContract.ItemEntry.CONTENT_URI,
				projectionItems, selection, selectionArgs,
				RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");

		cursor.moveToFirst();

		ArrayList<RoutineItem> itemsList = new ArrayList<>();

		for (int i = 0; i < cursor.getCount(); i++) {
			long id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));
			String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
			int itemLength = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
			int itemAvg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));

			RoutineItem newRoutine = new RoutineItem(itemName, itemLength, itemAvg);
			newRoutine.setmElapsedTime(0);
			newRoutine.setStartTime(0);
			newRoutine.setmRemainingTime(0);
			newRoutine.setmId(id);
			itemsList.add(newRoutine);
			if (!cursor.moveToNext()) break;
		}
		mRoutineItems = itemsList;
	}

	private long saveAsNewRoutine(Context context) {
		// Make CV for the routine
		ContentValues values = new ContentValues();
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, rName + " clone");
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, rItemsNumber);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, rRoutineLength);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME, rEndTime);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_CARRY, rCarryTime);
		values.put(RoutineContract.RoutineEntry.COLUMN_CURRENT_ITEM, -1);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_INTERRUPT_TIME, 0);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED, rTimesUsed);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END, rEndTimeReq);
		values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG, rRule);

		// Insert new routine
		Uri newUri = context.getContentResolver().insert(RoutineContract.RoutineEntry.CONTENT_URI, values);
		// Get info for items
		long newRoutineId = ContentUris.parseId(newUri);

		if (newUri != null) {
			// Insert items
			for (int i = 0; i < mRoutineItems.size(); i++) {
				ContentValues itemValues = new ContentValues();
				itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mRoutineItems.get(i).getmItemName());
				itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
				itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, mRoutineItems.get(i).getmTime());
				itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, mRoutineItems.get(i).getmTime());
				itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, newRoutineId);
				itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME, mRoutineItems.get(i).getmAverageTime());
				itemValues.put(RoutineContract.ItemEntry.COLUMN_ELAPSED_TIME, 0);
				itemValues.put(RoutineContract.ItemEntry.COLUMN_START_TIME, 0);


				context.getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
			}
			return ContentUris.parseId(newUri);
		}
		return 0;
	}

}

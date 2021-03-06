package com.sxnwlfkk.dailyroutines.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cs on 2017.02.17..
 */

public class RoutineContract {

	public static final String CONTENT_AUTHORITY = "com.sxnwlfkk.dailyroutines";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	public static final String PATH_ROUTINES = "routines";
	public static final String PATH_ITEMS = "items";

	public RoutineContract() {
	}

	public static abstract class RoutineEntry implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ROUTINES);

		public static final String TABLE_NAME = "routines";

		public static final String _ID = BaseColumns._ID;
		// Name of the routine
		public static final String COLUMN_ROUTINE_NAME = "name";
		// Preferred end time of routine
		public static final String COLUMN_ROUTINE_END_TIME = "end_time";
		// Requires wake up
		public static final String COLUMN_ROUTINE_REQUIRE_END = "require_end";
		// Length of the routine in seconds
		public static final String COLUMN_ROUTINE_LENGTH = "length";
		// Carry time in seconds
		public static final String COLUMN_ROUTINE_CARRY = "carry_time";
		// Number of associated routine items
		public static final String COLUMN_ROUTINE_ITEMS_NUMBER = "items_num";
		// Index of current item, if any
		public static final String COLUMN_CURRENT_ITEM = "curr_item";
		// Number of times used
		public static final String COLUMN_ROUTINE_TIMES_USED = "times_used";
		// Time of writing to DB in UTC seconds, when routine is interrupted
		public static final String COLUMN_ROUTINE_INTERRUPT_TIME = "interrupt_time";
		// Number of routine in the main order
		public static final String COLUMN_ROUTINE_ORDER_NUMBER = "order_number";
		// Weekdays config string for notifications
		public static final String COLUMN_ROUTINE_WEEKDAYS_CONFIG = "weekdays_config";
		// Extra text column in the DB for posterity
		public static final String COLUMN_ROUTINE_EXTRA_TEXT = "extra_text";
		// Extra long column in the DB for posterity
		public static final String COLUMN_ROUTINE_EXTRA_LONG = "extra_long";

		// Creating this table
		public static final String CREATE_RITUAL_TABLE = "CREATE TABLE " +
				                                                 TABLE_NAME + " ("
				                                                 + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				                                                 + COLUMN_ROUTINE_NAME + " TEXT NOT NULL, "
				                                                 + COLUMN_ROUTINE_END_TIME + " INTEGER NOT NULL DEFAULT 0,"
				                                                 + COLUMN_ROUTINE_REQUIRE_END + " INTEGER NOT NULL DEFAULT 0, "
				                                                 + COLUMN_ROUTINE_LENGTH + " INTEGER NOT NULL, "
				                                                 + COLUMN_ROUTINE_CARRY + " INTEGER NOT NULL DEFAULT 0, "
				                                                 + COLUMN_CURRENT_ITEM + " INTEGER NOT NULL DEFAULT -1, "
				                                                 + COLUMN_ROUTINE_ITEMS_NUMBER + " INTEGER NOT NULL, "
				                                                 + COLUMN_ROUTINE_INTERRUPT_TIME + " INTEGER NOT NULL DEFAULT 0, "
				                                                 + COLUMN_ROUTINE_ORDER_NUMBER + " INTEGER NOT NULL DEFAULT 0, "
				                                                 + COLUMN_ROUTINE_WEEKDAYS_CONFIG + " TEXT NOT NULL DEFAULT '', "
				                                                 + COLUMN_ROUTINE_EXTRA_TEXT + " TEXT NOT NULL DEFAULT '', "
				                                                 + COLUMN_ROUTINE_EXTRA_LONG + " INTEGER NOT NULL DEFAULT 0, "
				                                                 + COLUMN_ROUTINE_TIMES_USED + " INTEGER NOT NULL DEFAULT 0);";

		// Deleting table
		public static final String DELETE_RITUAL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	public static abstract class ItemEntry implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

		public static final String TABLE_NAME = "items";

		// ID if the item
		public static final String _ID = BaseColumns._ID;
		// ID of the parent routine
		public static final String COLUMN_PARENT_ROUTINE = "routine_id";
		// The place of item in the order of the routine
		public static final String COLUMN_ITEM_NO = "item_no";
		// Name of the item
		public static final String COLUMN_ITEM_NAME = "name";
		// Length of the item in seconds
		public static final String COLUMN_ITEM_LENGTH = "length";
		// Remaining time in item
		public static final String COLUMN_REMAINING_TIME = "remaining_time";
		// Elapsed time (it's needed, because the user can linger more on one
		// item, than the allotted time
		public static final String COLUMN_ELAPSED_TIME = "elapsed_time";
		// Start time to calculate vibration times
		public static final String COLUMN_START_TIME = "start_time";
		// Average time this item takes
		public static final String COLUMN_ITEM_AVG_TIME = "item_avg";

		// Creating this table
		public static final String CREATE_ITEM_TABLE = "CREATE TABLE "
				                                               + TABLE_NAME + " ("
				                                               + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				                                               + COLUMN_PARENT_ROUTINE + " INTEGER NOT NULL, "
				                                               + COLUMN_ITEM_NO + " INTEGER NOT NULL, "
				                                               + COLUMN_ITEM_NAME + " TEXT NOT NULL, "
				                                               + COLUMN_ITEM_LENGTH + " INTEGER NOT NULL DEFAULT 0, "
				                                               + COLUMN_REMAINING_TIME + " INTEGER NOT NULL DEFAULT 0, "
				                                               + COLUMN_ELAPSED_TIME + " INTEGER NOT NULL DEFAULT 0, "
				                                               + COLUMN_START_TIME + " INTEGER NOT NULL DEFAULT 0, "
				                                               + COLUMN_ITEM_AVG_TIME + " INTEGER NOT NULL DEFAULT 0);";

		// Deleting table
		public static final String DELETE_ITEM_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

}

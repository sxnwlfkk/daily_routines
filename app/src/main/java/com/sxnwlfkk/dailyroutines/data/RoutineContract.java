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

    public RoutineContract() {}

    public static abstract class RoutineEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ROUTINES);

        public static final String TABLE_NAME = "routines";

        public static final String _ID = BaseColumns._ID;
        // Name of the routine
        public static final String COLUMN_ROUTINE_NAME = "name";
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

        // Creating this table
        public static final String CREATE_RITUAL_TABLE = "CREATE TABLE " +
                TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ROUTINE_NAME + " TEXT NOT NULL, "
                + COLUMN_ROUTINE_LENGTH + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ROUTINE_CARRY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_CURRENT_ITEM + " INTEGER NOT NULL DEFAULT -1, "
                + COLUMN_ROUTINE_ITEMS_NUMBER + " INTEGER NOT NULL, "
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
        // Average time this item takes
        public static final String COLUMN_ITEM_AVG_TIME = "items_num";

        // Creating this table
        public static final String CREATE_ITEM_TABLE = "CREATE TABLE "
                + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PARENT_ROUTINE + " INTEGER NOT NULL, "
                + COLUMN_ITEM_NO + " INTEGER NOT NULL, "
                + COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + COLUMN_ITEM_LENGTH + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_REMAINING_TIME + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ITEM_AVG_TIME + " INTEGER NOT NULL DEFAULT 0);";

        // Deleting table
        public static final String DELETE_ITEM_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}

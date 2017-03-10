package com.sxnwlfkk.dailyrituals.data;

import android.provider.BaseColumns;

/**
 * Created by cs on 2017.02.17..
 */

public class RitualContract {

    public RitualContract() {}

    public static abstract class RitualEntry implements BaseColumns {

        public static final String TABLE_NAME = "rituals";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_RITUAL_NAME = "name";
        public static final String COLUMN_RITUAL_LENGTH = "length";
        public static final String COLUMN_CURRENT_ITEM = "curr_item";
        public static final String COLUMN_RITUAL_ITEMS = "items";

        // Creating this table
        public static final String CREATE_RITUAL_TABLE = "CREATE TABLE " +
                TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RITUAL_NAME + " TEXT NOT NULL, "
                + COLUMN_RITUAL_LENGTH + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_CURRENT_ITEM + " INTEGER NOT NULL DEFAULT -1, "
                + COLUMN_RITUAL_ITEMS + " TEXT NOT NULL);";

        // Deleting table
        public static final String DELETE_RITUAL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}

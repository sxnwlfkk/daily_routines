package com.sxnwlfkk.dailyroutines.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cs on 2017.02.17..
 */

public class RoutineDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = RoutineDbHelper.class.getSimpleName();

    /* DN name */
    private  static final String DATABASE_NAME = "rituals.db";

    /* DB version, if you change the schema, you need to increment version number. */
    private static final int DATABASE_VERSION = 1;

    public RoutineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RoutineContract.RoutineEntry.CREATE_RITUAL_TABLE);
        db.execSQL(RoutineContract.ItemTable.CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RoutineContract.RoutineEntry.DELETE_RITUAL_TABLE);
        db.execSQL(RoutineContract.ItemTable.DELETE_ITEM_TABLE);

        onCreate(db);
    }
}

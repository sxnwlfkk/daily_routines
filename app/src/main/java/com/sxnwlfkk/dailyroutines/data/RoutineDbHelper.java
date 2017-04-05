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
        db.execSQL(RoutineContract.RitualEntry.CREATE_RITUAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

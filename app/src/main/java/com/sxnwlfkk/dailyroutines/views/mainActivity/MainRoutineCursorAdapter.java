package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by cs on 2017.04.05..
 */

public class MainRoutineCursorAdapter extends CursorAdapter {

    // Constructor
    public MainRoutineCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}

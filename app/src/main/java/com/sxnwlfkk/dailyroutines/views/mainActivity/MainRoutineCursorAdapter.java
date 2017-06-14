package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;

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
        return LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.main_name_field);
        TextView tvLength = (TextView) view.findViewById(R.id.main_length_field);
        TextView tvStartTime = (TextView) view.findViewById(R.id.main_start_time_text);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
        int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
        boolean endRequired = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END)) == 1;
        if (endRequired) {
            int endTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));
            tvStartTime.setText(RoutineUtils.formatClockTimeString(RoutineUtils.calculateIdealStartTime(endTime, length) / 1000));
        } else {
            tvStartTime.setVisibility(View.INVISIBLE);
        }

        tvName.setText(name);
        tvLength.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(length)));
    }
}

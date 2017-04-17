package com.sxnwlfkk.dailyroutines.views.profileActivity;

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
 * Created by cs on 2017.04.07..
 */

public class ProfileCursorAdapter extends CursorAdapter {


    public ProfileCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.profile_list_name);
        TextView tvLength = (TextView) view.findViewById(R.id.profile_list_length);
        TextView tvAvg = (TextView) view.findViewById(R.id.profile_list_item_avg);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
        int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
        int avg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));

        tvName.setText(name);
        tvLength.setText(RoutineUtils.formatTimeString(length));
        tvAvg.setText(RoutineUtils.formatTimeString(avg));
    }
}

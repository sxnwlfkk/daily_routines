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
        TextView itemNo = (TextView) view.findViewById(R.id.profile_list_number);
        itemNo.setVisibility(View.GONE);

        TextView tvName = (TextView) view.findViewById(R.id.profile_list_name);
        TextView tvLength = (TextView) view.findViewById(R.id.profile_list_length);
        TextView tvAvg = (TextView) view.findViewById(R.id.profile_list_item_avg);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
        int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
        int avg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));

        // Setting average cell bacground for visual information conveying
        int relation = RoutineUtils.decideAvgColor(length, avg);
        switch (relation) {
            case RoutineUtils.AVERAGE_NIL_OR_EQ:
                break;
            case RoutineUtils.AVERAGE_BIGGER:
                tvAvg.setBackgroundColor(context.getResources().getColor(R.color.material_red_lighten1));
                break;
            case RoutineUtils.AVERAGE_SMALLER:
                tvAvg.setBackgroundColor(context.getResources().getColor(R.color.material_teal_lighten3));
        }

        tvName.setText(name);
        tvLength.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(length)));
        tvAvg.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(avg)));
    }
}

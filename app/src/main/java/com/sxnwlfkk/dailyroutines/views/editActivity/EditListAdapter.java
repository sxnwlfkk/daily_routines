package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.util.RoutineUtils;

import java.util.List;

/**
 * Created by cs on 2017.04.09..
 */

public class EditListAdapter extends ArrayAdapter<RoutineItem> {

    public EditListAdapter(@NonNull Context context, List<RoutineItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.profile_list_item, parent, false
            );
        }

        RoutineItem rItem = getItem(position);

        TextView nameView = (TextView) listItemView.findViewById(R.id.profile_list_name);
        nameView.setText(rItem.getmItemName());

        TextView lengthView = (TextView) listItemView.findViewById(R.id.profile_list_length);
        lengthView.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(rItem.getmTime())));

        TextView avgView = (TextView) listItemView.findViewById(R.id.profile_list_item_avg);
        if (rItem.getmAverageTime() < 0) {
            avgView.setText(R.string.edit_list_view_composite_avg_field);
        } else {
            avgView.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec((long) rItem.getmAverageTime())));
        }

        TextView itemNo = (TextView) listItemView.findViewById(R.id.profile_list_number);
        itemNo.setText((position + 1) + ".");

        // Setting average cell background for visual information conveying
        if (rItem.getmAverageTime() < 0) {
            LinearLayout ll = (LinearLayout) listItemView.findViewById(R.id.edit_list_item_background);
            ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_indigo_lighten5));
        } else {
            int relation = RoutineUtils.decideAvgColor(rItem.getmTime(), (int) rItem.getmAverageTime());
            switch (relation) {
                case RoutineUtils.AVERAGE_NIL_OR_EQ:
                    break;
                case RoutineUtils.AVERAGE_BIGGER:
                    avgView.setBackgroundColor(getContext().getResources().getColor(R.color.material_red_lighten1));
                    break;
                case RoutineUtils.AVERAGE_SMALLER:
                    avgView.setBackgroundColor(getContext().getResources().getColor(R.color.material_teal_lighten3));
            }
        }

        return listItemView;
    }
}

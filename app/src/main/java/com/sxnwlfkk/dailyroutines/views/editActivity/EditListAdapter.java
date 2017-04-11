package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;

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
                    R.layout.edit_list_item, parent, false
            );
        }

        RoutineItem rItem = getItem(position);

        TextView nameView = (TextView) listItemView.findViewById(R.id.edit_list_name_text);
        nameView.setText(rItem.getmItemName());

        TextView lengthView = (TextView) listItemView.findViewById(R.id.edit_list_time_text);
        lengthView.setText(String.valueOf(rItem.getmTime()));

        TextView avgView = (TextView) listItemView.findViewById(R.id.edit_list_avg_text);
        avgView.setText(String.valueOf(rItem.getmAverageTime()));

        return listItemView;
    }
}

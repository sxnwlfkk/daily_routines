package com.sxnwlfkk.dailyrituals.views.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sxnwlfkk.dailyrituals.R;
import com.sxnwlfkk.dailyrituals.classes.Ritual;

import java.util.List;

/**
 * Created by cs on 2017.01.31..
 */

public class RitualAdapter extends ArrayAdapter<Ritual> {

    public RitualAdapter (Context context, List<Ritual> rituals) {
        super(context, 0, rituals);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.main_list_item, parent, false);
        }

        Ritual currentRitual = getItem(position);

        TextView nameView = (TextView) listItemView.findViewById(R.id.name_field);
        nameView.setText(currentRitual.getmRitualName());

        TextView lengthView = (TextView) listItemView.findViewById(R.id.length_field);
        lengthView.setText(currentRitual.getmRitualLength() + " sec.");

        return listItemView;
    }
}

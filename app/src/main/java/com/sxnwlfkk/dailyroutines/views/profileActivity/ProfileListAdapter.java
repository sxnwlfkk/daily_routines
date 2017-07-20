package com.sxnwlfkk.dailyroutines.views.profileActivity;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
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
 * Created by sxnwlfkk on 2017.07.18..
 */

public class ProfileListAdapter extends ArrayAdapter<RoutineItem> {


    public ProfileListAdapter(@NonNull Context context, List<RoutineItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.profile_list_item, parent, false
            );
        }

        RoutineItem rItem = getItem(position);

        TextView itemNoView = (TextView) view.findViewById(R.id.profile_list_number);
        TextView tvName = (TextView) view.findViewById(R.id.profile_list_name);
        TextView tvLength = (TextView) view.findViewById(R.id.profile_list_length);
        TextView tvAvg = (TextView) view.findViewById(R.id.profile_list_item_avg);

        String name = rItem.getmItemName();
        long length = rItem.getmTime();
        long avg = (long) rItem.getmAverageTime();
        int itemNo = position;
        int itemTier = rItem.getmTier();

        itemNoView.setText(itemNo + 1 + ".");

        LinearLayout ll = (LinearLayout) view.findViewById(R.id.edit_list_item_background);
        if (itemTier == 0) {
            ll.setBackgroundColor(getContext().getResources().getColor(R.color.bpTransparent));
        } else {
            int tier = itemTier % 8;
            switch (tier) {
                case 1:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier1));
                    break;
                case 2:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier2));
                    break;
                case 3:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier3));
                    break;
                case 4:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier4));
                    break;
                case 5:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier5));
                    break;
                case 6:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier6));
                    break;
                case 7:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier7));
                    break;
                case 0:
                    ll.setBackgroundColor(getContext().getResources().getColor(R.color.material_tier8));
                    break;
            }
        }


        // Setting average cell bacground for visual information conveying
        int relation = RoutineUtils.decideAvgColor(length, avg);
        switch (relation) {
            case RoutineUtils.AVERAGE_NIL_OR_EQ:
                break;
            case RoutineUtils.AVERAGE_BIGGER:
                tvAvg.setBackgroundColor(getContext().getResources().getColor(R.color.material_red_lighten1));
                break;
            case RoutineUtils.AVERAGE_SMALLER:
                tvAvg.setBackgroundColor(getContext().getResources().getColor(R.color.material_teal_lighten3));
        }

        tvName.setText(name);
        tvLength.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(length)));
        tvAvg.setText(RoutineUtils.formatLengthString(RoutineUtils.msecToSec(avg)));

        return view;
    }
}

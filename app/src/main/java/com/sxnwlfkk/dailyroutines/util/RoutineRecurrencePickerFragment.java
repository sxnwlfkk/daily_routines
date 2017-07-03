package com.sxnwlfkk.dailyroutines.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;
import com.sxnwlfkk.dailyroutines.R;

/**
 * Created by sxnwlfkk on 2017.07.03..
 */

public class RoutineRecurrencePickerFragment extends RecurrencePickerDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = super.onCreateView(inflater, container, savedInstanceState);

        Spinner mFreqSpinner = (Spinner) mView.findViewById(R.id.freqSpinner);
        mFreqSpinner.setVisibility(View.GONE);
        LinearLayout mIntervalBox = (LinearLayout) mView.findViewById(R.id.intervalGroup);
        mIntervalBox.setVisibility(View.GONE);
        Spinner mEndSpinner = (Spinner) mView.findViewById(R.id.endSpinner);
        mEndSpinner.setVisibility(View.GONE);
        EditText mEndCount = (EditText) mView.findViewById(R.id.endCount);
        mEndCount.setVisibility(View.GONE);

        return mView;
    }
}

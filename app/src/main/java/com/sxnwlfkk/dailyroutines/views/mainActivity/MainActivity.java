package com.sxnwlfkk.dailyroutines.views.mainActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sxnwlfkk.dailyroutines.R;

public class MainActivity extends Activity {

    ListView mRoutineListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRoutineListView = (ListView) findViewById(R.id.main_list);
        TextView mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mRoutineListView.setEmptyView(mEmptyStateTextView);


        /* Set up FAB */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_ritual);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


    }
}


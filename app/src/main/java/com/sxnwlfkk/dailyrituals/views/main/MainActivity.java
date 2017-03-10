package com.sxnwlfkk.dailyrituals.views.main;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sxnwlfkk.dailyrituals.R;
import com.sxnwlfkk.dailyrituals.classes.Ritual;
import com.sxnwlfkk.dailyrituals.classes.RitualItem;
import com.sxnwlfkk.dailyrituals.data.RitualDbHelper;
import com.sxnwlfkk.dailyrituals.data.RitualContract.RitualEntry;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ArrayList<Ritual> mRitualList = new ArrayList<>();
    ListView mRitualListView;
    RitualAdapter mAdapter;

    int testCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRitualListView = (ListView) findViewById(R.id.main_list);
        TextView mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mRitualListView.setEmptyView(mEmptyStateTextView);


        /* Set up FAB */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_ritual);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTestItemToDb();
                loadDatabase();
                setupListView();
            }
        });

        mAdapter = new RitualAdapter(this, new ArrayList<Ritual>());
        loadDatabase();
        setupListView();

        if (mRitualList.isEmpty()) {
            mEmptyStateTextView.setText("No rituals yet. Add one with the watch icon!");
        }

    }

    private void addTestItemToDb() {
        RitualDbHelper mRitualHelper = new RitualDbHelper(this);
        SQLiteDatabase db = mRitualHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(RitualEntry.COLUMN_RITUAL_NAME, "Ritual test " + testCounter++);
        values.put(RitualEntry.COLUMN_RITUAL_ITEMS, 10);
        values.put(RitualEntry.COLUMN_RITUAL_ITEMS, "test");

        long newRowId = db.insert(RitualEntry.TABLE_NAME, null, values);
    }

    private void loadDatabase() {
        RitualDbHelper mDbHelper = new RitualDbHelper(this);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                RitualEntry._ID,
                RitualEntry.COLUMN_RITUAL_NAME,
                RitualEntry.COLUMN_RITUAL_LENGTH
        };

        Cursor cursor = db.query(
                RitualEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.getCount() == 0) return;

        try {

            int idCI = cursor.getColumnIndex(RitualEntry._ID);
            int nameCI = cursor.getColumnIndex(RitualEntry.COLUMN_RITUAL_NAME);
            //int lengthCI = cursor.getColumnIndex(RitualEntry.COLUMN_RITUAL_LENGTH);

            if (!mRitualList.isEmpty()) {
                mRitualList.clear();
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(idCI);
                String name = cursor.getString(nameCI);
                //int length = cursor.getInt(lengthCI);

                Ritual new_ritual = new Ritual(id, name, getListOfItems());
                mRitualList.add(new_ritual);
            }
        } finally {
            cursor.close();
        }

    }

    private void setupListView () {
        mAdapter.clear();

        if (mRitualList != null && !mRitualList.isEmpty()) {
            mAdapter.addAll(mRitualList);
        }

//        RitualAdapter ritualAdapter = new RitualAdapter(this, mRitualList);
        mRitualListView.setAdapter(mAdapter);
    }

    /**
     * Test function, setting up ritual lists.
     */
    private void setUpTestRitual () {
        String name = "Test ritual";
        ArrayList<RitualItem> ritualList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ritualList.add(new RitualItem(name + " item " + i, 60));
        }

        Ritual testRitual1 = new Ritual(1, name, ritualList);
        this.mRitualList.add(testRitual1);
    }

    /**
     * Test function, returns an ArrayList of RitualItems
     */
    private ArrayList<RitualItem> getListOfItems () {
        String name = "Test ritual";
        ArrayList<RitualItem> ritualList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ritualList.add(new RitualItem(name + " item " + i, 60));
        }
        return ritualList;
    }
}

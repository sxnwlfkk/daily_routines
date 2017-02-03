package com.sxnwlfkk.dailyrituals.views.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.sxnwlfkk.dailyrituals.R;
import com.sxnwlfkk.dailyrituals.data.Ritual;
import com.sxnwlfkk.dailyrituals.data.RitualItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    ArrayList<Ritual> mTestRitualList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build test list
        setUpTestRitual();

        // Setting up ListView
        ListView ritualListView = (ListView) findViewById(R.id.main_list);
        RitualAdapter ritualAdapter = new RitualAdapter(this, mTestRitualList);
        ritualListView.setAdapter(ritualAdapter);
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

        Ritual testRitual1 = new Ritual(name, 10, ritualList);
        mTestRitualList.add(testRitual1);
    }
}

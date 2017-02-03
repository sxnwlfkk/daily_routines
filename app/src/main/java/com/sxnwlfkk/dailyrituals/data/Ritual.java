package com.sxnwlfkk.dailyrituals.data;

import java.util.ArrayList;

/**
 * Created by cs on 2017.01.17..
 */

public class Ritual {

    private int mCarryTime;
    private String mRitualName;
    private int mRitualItemNum;
    private int mCurrentItem;
    private int mRitualLength;
    private ArrayList<RitualItem> mRitualItemList;
    /** Wakeup time in seconds. */
    private int mWakeupTime;
    private boolean mRequireWakeup;

    /**
     * Constructor to use when wakeup is not set.
     * @param ritual_name The name of the ritual.
     * @param ritual_item_num The item count of the ritual.
     * @param ritual_items An ArrayList of RitualItems.
     */
    public Ritual (String ritual_name, int ritual_item_num, ArrayList<RitualItem> ritual_items) {
        mRitualName = ritual_name;
        mRitualItemNum = ritual_item_num;
        mCurrentItem = 0;
        mRitualItemList = ritual_items;
        mRequireWakeup = false;
        mRitualLength = countRitualLength(ritual_items);
    }

    /**
     * Constructor to use, when wakeup is needed.
     * @param ritual_name The name of the ritual.
     * @param ritual_item_num The item count of the ritual.
     * @param ritual_items An ArrayList of RitualItems.
     * @param wakeup_time The wakeup time in seconds from midnight.
     */
    public Ritual (String ritual_name, int ritual_item_num, ArrayList<RitualItem> ritual_items, int wakeup_time) {
        mRitualName = ritual_name;
        mRitualItemNum = ritual_item_num;
        mCurrentItem = 0;
        mRitualItemList = ritual_items;
        mRequireWakeup = true;
        mWakeupTime = wakeup_time;

    }

    public void distributeCarryLoss () {

    }

    /**
     * Returns the length of all ritualItems in the array list.
     * @param ritualItems ArrayList of Ritual Items
     * @return
     */
    private int countRitualLength(ArrayList<RitualItem> ritualItems) {
        int length = 0;
        for (RitualItem ritualItem : ritualItems) {
            length += ritualItem.getmTime();
        }
        return length;
    }

    /* GETTERS */
    public int getmCarryTime() {
        return mCarryTime;
    }


    public int getmRitualItemNum() {
        return mRitualItemNum;
    }

    public int getmCurrentItem() {
        return mCurrentItem;
    }

    public int getmRitualLength() {
        return mRitualLength;
    }

    public String getmRitualName() {
        return mRitualName;
    }

    public ArrayList<RitualItem> getmRitualItemList() {
        return mRitualItemList;
    }

    public boolean ismRequireWakeup() {
        return mRequireWakeup;
    }

    public int getmWakeupTime() {
        return mWakeupTime;
    }
}

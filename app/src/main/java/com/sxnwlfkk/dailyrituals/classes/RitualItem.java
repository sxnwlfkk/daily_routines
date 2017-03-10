package com.sxnwlfkk.dailyrituals.classes;

/**
 * Created by cs on 2017.01.17..
 */

public class RitualItem {

    private String mItemName;
    private int mTime;
    private int mCurrentTime;
    private double mAverageTime;

    /**
     * New object with no prior average data.
     * @param item_name
     * @param time
     */
    public RitualItem (String item_name, int time) {
        this.mItemName = item_name;
        this.mTime = this.mCurrentTime = time;
        this.mAverageTime = 0;
    }

    /**
     * Constructor when editing an existing RitualItem object. Should provide prior averages data,
     * if there are any.
     * @param item_name
     * @param time
     * @param average
     */
    public RitualItem (String item_name, int time, double average) {
        this.mItemName = item_name;
        this.mTime = this.mCurrentTime = time;
        this.mAverageTime = average;
    }

    /**
     * Reverts the item to original state and average the runtime with the previous runs.
     */
    public void resetItem () {
        this.mAverageTime = (this.mAverageTime + (double) this.mCurrentTime) / 2.0;
        this.mCurrentTime = this.mTime;
    }

    /* GETTERS */
    public int getmCurrentTime() {
        return mCurrentTime;
    }

    public String getmItemName() {
        return mItemName;
    }

    public int getmTime() {
        return mTime;
    }

    public double getmAverageTime() {
        return mAverageTime;
    }

    /* SETTERS */
    public void setmCurrentTime(int mCurrentTime) {
        this.mCurrentTime = mCurrentTime;
    }

    public void setmAverageTime(double mAverageTime) {
        this.mAverageTime = mAverageTime;
    }
}

package com.sxnwlfkk.dailyroutines.classes;

/**
 * Created by cs on 2017.01.17..
 */

public class RoutineItem {

    private String mItemName;

    private int mTime;

    private int mCurrentTime;

    private double mAverageTime;

    private int mElapsedTime;


    private long mId;

    /**
     * New object with no prior average data.
     * @param item_name
     * @param time
     */
    public RoutineItem(String item_name, int time) {
        mItemName = item_name;
        mTime = this.mCurrentTime = time;
        mAverageTime = 0;
        mElapsedTime = 0;
    }
    /**
     * Constructor when editing an existing RoutineItem object. Should provide prior averages data,
     * if there are any.
     * @param item_name
     * @param time
     * @param average
     */
    public RoutineItem(String item_name, int time, double average) {
        mItemName = item_name;
        mTime = this.mCurrentTime = time;
        mAverageTime = average;
        mElapsedTime = 0;
    }

    /**
     * Reverts the item to original state and averages the runtime with the previous runs.
     */
    public void resetItem () {
        mCurrentTime = mTime;
        mElapsedTime = 0;
    }

    public void averageItemTime() {
        mAverageTime = (mAverageTime + (double) mElapsedTime) / 2.0;
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

    public long getmId() {
        return mId;
    }

    public int getmElapsedTime() {
        return mElapsedTime;
    }

    /* SETTERS */
    public void setmCurrentTime(int currentTime) {
        mCurrentTime = currentTime;
    }

    public void setmTime(int mTime) {
        this.mTime = mTime;
    }

    public void setmItemName(String mItemName) {
        this.mItemName = mItemName;
    }

    public void setmAverageTime(double mAverageTime) {
        this.mAverageTime = mAverageTime;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public void setmElapsedTime(int mElapsedTime) {
        this.mElapsedTime = mElapsedTime;
    }

    public void incrementElapsedTime() { mElapsedTime++; }
}

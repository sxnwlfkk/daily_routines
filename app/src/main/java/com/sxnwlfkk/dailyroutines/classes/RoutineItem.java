package com.sxnwlfkk.dailyroutines.classes;

/**
 * Created by cs on 2017.01.17..
 */

public class RoutineItem {

    private String mItemName;

    private long mTime;

    private long mCurrentTime;

    private double mAverageTime;

    private long mElapsedTime;

    private long mRemainingTime;

    private int mTier;

    private long mParent;

    private long mId;

    private int mItemNumber;

    private long startTime;

    /**
     * New object with no prior average data.
     * @param item_name
     * @param time
     */
    public RoutineItem(String item_name, long time) {
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
    public RoutineItem(String item_name, long time, double average) {
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
        startTime = 0;
    }

    public void averageItemTime() {
        if (mAverageTime == 0) {
            mAverageTime = mElapsedTime;
        } else {
            mAverageTime = (mAverageTime + (double) mElapsedTime) / 2.0;
        }
        startTime = 0;
        mElapsedTime = 0;
    }

    /* GETTERS */
    public long getmCurrentTime() {
        return mCurrentTime;
    }

    public String getmItemName() {
        return mItemName;
    }

    public long getmTime() {
        return mTime;
    }

    public double getmAverageTime() {
        return mAverageTime;
    }

    public long getmId() {
        return mId;
    }

    public long getmElapsedTime() {
        return mElapsedTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getmTier() {
        return mTier;
    }

    public long getmParent() {
        return mParent;
    }

    public int getmItemNumber() {
        return mItemNumber;
    }

    /* SETTERS */
    public void setmCurrentTime(long currentTime) {
        mCurrentTime = currentTime;
    }

    public void setmTime(long mTime) {
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

    public void setmElapsedTime(long mElapsedTime) {
        this.mElapsedTime = mElapsedTime;
    }

    public void incrementElapsedTime(long i) { mElapsedTime += i; }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getmRemainingTime() {
        return mRemainingTime;
    }

    public void setmRemainingTime(long mRemainingTime) {
        this.mRemainingTime = mRemainingTime;
    }

    public void setmTier(int mTier) {
        this.mTier = mTier;
    }

    public void setmParent(long mParent) {
        this.mParent = mParent;
    }

    public void setmItemNumber(int mItemNumber) {
        this.mItemNumber = mItemNumber;
    }

}

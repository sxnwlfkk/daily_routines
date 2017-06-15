package com.sxnwlfkk.dailyroutines.classes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cs on 2017.04.14..
 */

public class RoutineClock {

    public static final String LOG_TAG = RoutineClock.class.getSimpleName();

    // VARS
    private ArrayList<RoutineItem> mItemsList;
    private String mName;
    private int mCurrentItemIndex;
    private long mCarryTime;
    private int mRoutineItemsNum;
    private long mLength;
    private long mEndTime;


    private boolean mEndTimeRequired;
    private long mId;
    private int mTimesUsed;
    private long mDiffTime;

    // Distribute carry time
    // Offset is the number, which the counter should be modified from the current item
    // If the routine is started late, we should use 0 offset, for the algorithm to distribute
    // the carry time among all the items. When a routine is underway, and the user goes to red
    // we should use 1 as offset, to distribute among the remaining items.
    private void distributeCarryTime(int offset) {
        long remainingTime = 0;
        for (int i = mCurrentItemIndex+offset; i < mItemsList.size(); i++) {
            remainingTime += mItemsList.get(i).getmCurrentTime();
        }
        Log.e(LOG_TAG, "Remaining time: " + remainingTime);
        Log.e(LOG_TAG, "Carry time: " + mCarryTime);
        for (int i = mCurrentItemIndex+offset; i < mItemsList.size(); i++) {
            RoutineItem item = mItemsList.get(i);
            double ratio = (double) item.getmCurrentTime() / remainingTime;
            double sub = ratio * mCarryTime;
            long oldItemTime = item.getmCurrentTime();
            long newItemTime = (long) (oldItemTime + sub);
            item.setmCurrentTime(newItemTime);
            mItemsList.set(i, item);
            Log.e(LOG_TAG, i + "-th new item time is: " + newItemTime);
        }
        mCarryTime = 0;
    }

    // Distribute carry on start
    public void distributeCarryOnStart(long carry) {
        mCarryTime = carry;
        if (carry < 0) {
            distributeCarryTime(0);
        }
    }

    // Next item
    public RoutineItem nextItem(RoutineItem currentItem) {
        //Sanity check
        if (mCurrentItemIndex + 1 >= mRoutineItemsNum) return null;

        setStartTime();

        // Time check
        if (currentItem.getmCurrentTime() == 0 && mCarryTime < 0) {
            distributeCarryTime(1);
        } else if (currentItem.getmCurrentTime() > 0) {
            mCarryTime += currentItem.getmCurrentTime();
            currentItem.setmCurrentTime(0);
        }

        mItemsList.set(mCurrentItemIndex, currentItem);
        mCurrentItemIndex++;
        setStartTime();
        return mItemsList.get(mCurrentItemIndex);
    }

    // Previous item
    public RoutineItem prevItem(RoutineItem currentItem) {
        // Sanity check
        if (mCurrentItemIndex - 1 < 0) return null;

        // Save current item state, and return previous
        mItemsList.set(mCurrentItemIndex, currentItem);
        mCurrentItemIndex--;

        // Try to take back as much carry as was left in item time
        long lastTime = mItemsList.get(mCurrentItemIndex).getStartTime();
        if (lastTime < mCarryTime) {
            mItemsList.get(mCurrentItemIndex).setmCurrentTime(lastTime);
            mCarryTime -= lastTime;
        } else if (0 < mCarryTime && mCarryTime <= lastTime) {
            mItemsList.get(mCurrentItemIndex).setmCurrentTime(mCarryTime);
            mCarryTime = 0;
        }

        setStartTime();
        return mItemsList.get(mCurrentItemIndex);
    }

    // Set the routines start time
    public void setStartTime() {
        mItemsList.get(mCurrentItemIndex).setStartTime(mItemsList.get(mCurrentItemIndex).getmCurrentTime());
    }

    // Reset routine
    public void resetRoutine() {
        mCarryTime = 0;
        mCurrentItemIndex = -1;

        // Clear items
        for (int i = 0; i < mItemsList.size(); i++) {
            RoutineItem item = mItemsList.get(i);
            item.resetItem();
            mItemsList.set(i, item);
        }

    }

    // Finish routine
    public void finishRoutine() {
        mCarryTime = 0;
        mCurrentItemIndex = -1;
        mTimesUsed++;

        for (int i = 0; i < mItemsList.size(); i++) {
            RoutineItem item = mItemsList.get(i);
            item.averageItemTime();
            item.resetItem();
            mItemsList.set(i, item);
        }
    }

    // Sort out diff time
    public void sortDiffTime() {
        if (mDiffTime != 0) {
            long currTime = mItemsList.get(mCurrentItemIndex).getmCurrentTime();
            long itemsElapsedTime = mItemsList.get(mCurrentItemIndex).getmElapsedTime();
            if (mLength < mDiffTime) {
                mItemsList.get(mCurrentItemIndex).setmElapsedTime(itemsElapsedTime + mLength);
                mLength = 0;
                return;
            }
            mItemsList.get(mCurrentItemIndex).setmElapsedTime(itemsElapsedTime + mDiffTime);
            if (mDiffTime > currTime) {
                mItemsList.get(mCurrentItemIndex).setmCurrentTime(0);
                mCarryTime -= (mDiffTime - currTime);
            } else {
                mItemsList.get(mCurrentItemIndex).setmCurrentTime(currTime - mDiffTime);
            }
            calculateRemainingRoutineTime();
        }
    }

    // Calculate the remaining time
    public void calculateRemainingRoutineTime() {
        long newLength = mCarryTime;
        for (int i = mCurrentItemIndex; i < mRoutineItemsNum; i++) {
            newLength += mItemsList.get(i).getmCurrentTime();
        }
        mLength = newLength;
    }



    // Getters and Setters

    public RoutineItem getCurrentItem() {
        if (mCurrentItemIndex == -1) mCurrentItemIndex = 0;
        return mItemsList.get(mCurrentItemIndex);
    }

    public void setCurrentItem(RoutineItem item) {
        mItemsList.set(mCurrentItemIndex, item);

    }

    public int getmRoutineItemsNum() {
        return mRoutineItemsNum;
    }

    public void setmRoutineItemsNum(int mRoutineItemsNum) {
        this.mRoutineItemsNum = mRoutineItemsNum;
    }

    public ArrayList<RoutineItem> getmItemsList() {
        return mItemsList;
    }

    public void setmItemsList(ArrayList<RoutineItem> itemsList) {
        int routineLen = 0;
        int remTime = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            remTime += itemsList.get(i).getmCurrentTime();
        }

        mLength = remTime;
        mItemsList = itemsList;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmCurrentItemIndex() {
        return mCurrentItemIndex;
    }

    public void setmCurrentItemIndex(int currentItemIndex) {
        if (currentItemIndex == -1) {
            Log.e("RoutineClock", "setmCurrentItemIndex");
            mCurrentItemIndex = 0;
        }
        else {
            mCurrentItemIndex = currentItemIndex;
        }
    }

    public long getmCarryTime() {
        return mCarryTime;
    }

    public void setmCarryTime(long mCarryTime) {
        this.mCarryTime = mCarryTime;
    }

    public long getmLength() {
        return mLength;
    }

    public void setmLength(long mLength) {
        this.mLength = mLength;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public long getmEndTime() {
        return mEndTime;
    }

    public void setmEndTime(long mEndTime) {
        this.mEndTime = mEndTime;
    }

    public int getmTimesUsed() {
        return mTimesUsed;
    }

    public void setmTimesUsed(int mTimesUsed) {
        this.mTimesUsed = mTimesUsed;
    }

    public long getmDiffTime() {
        return mDiffTime;
    }

    public void setmDiffTime(long mDiffTime) {
        this.mDiffTime = mDiffTime;
    }

    public boolean ismEndTimeRequired() {
        return mEndTimeRequired;
    }

    public void setmEndTimeRequired(boolean mEndTimeRequired) {
        this.mEndTimeRequired = mEndTimeRequired;
    }

}

package com.sxnwlfkk.dailyroutines.classes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cs on 2017.04.14..
 */

public class RoutineClock {

    // VARS
    private ArrayList<RoutineItem> mItemsList;
    private String mName;
    private int mCurrentItemIndex;
    private int mCarryTime;
    private int mRoutineItemsNum;
    private int mLength;
    private int mEndTime;
    private long mId;
    private int mTimesUsed;
    private int mDiffTime;

    // Distribute carry time
    private void distibuteCarryTime() {
        int remainingTime = 0;
        for (int i = mCurrentItemIndex+1; i < mItemsList.size(); i++) {
            remainingTime += mItemsList.get(i).getmCurrentTime();
        }
        for (int i = mCurrentItemIndex+1; i < mItemsList.size(); i++) {
            RoutineItem item = mItemsList.get(i);
            float ratio = (float) item.getmCurrentTime() / remainingTime;
            float sub = ratio * mCarryTime;
            item.setmCurrentTime((int) (item.getmCurrentTime() + sub));
            mItemsList.set(i, item);
        }
        mCarryTime = 0;
    }

    // Next item
    public RoutineItem nextItem(RoutineItem currentItem) {
        //Sanity check
        if (mCurrentItemIndex + 1 >= mRoutineItemsNum) return null;

        setStartTime();

        // Time check
        if (currentItem.getmCurrentTime() == 0 && mCarryTime < 0) {
            distibuteCarryTime();
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
        int lastTime = mItemsList.get(mCurrentItemIndex).getStartTime();
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
            int currTime = mItemsList.get(mCurrentItemIndex).getmCurrentTime();
            int itemsElapsedTime = mItemsList.get(mCurrentItemIndex).getmElapsedTime();
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
        }
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

    public int getmCarryTime() {
        return mCarryTime;
    }

    public void setmCarryTime(int mCarryTime) {
        this.mCarryTime = mCarryTime;
    }

    public int getmLength() {
        return mLength;
    }

    public void setmLength(int mLength) {
        this.mLength = mLength;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public int getmEndTime() {
        return mEndTime;
    }

    public void setmEndTime(int mEndTime) {
        this.mEndTime = mEndTime;
    }

    public int getmTimesUsed() {
        return mTimesUsed;
    }

    public void setmTimesUsed(int mTimesUsed) {
        this.mTimesUsed = mTimesUsed;
    }

    public int getmDiffTime() {
        return mDiffTime;
    }

    public void setmDiffTime(int mDiffTime) {
        this.mDiffTime = mDiffTime;
    }

}

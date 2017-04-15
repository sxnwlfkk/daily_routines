package com.sxnwlfkk.dailyroutines.classes;

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


    // Distribute carry time
    private void distibuteCarryTime() {
        int remainingTime = 0;
        for (int i = mCurrentItemIndex+1; i < mItemsList.size(); i++) {
            remainingTime += mItemsList.get(i).getmCurrentTime();
        }
        for (int i = mCurrentItemIndex+1; i < mItemsList.size(); i++) {
            RoutineItem item = mItemsList.get(i);
            float ratio = (float) remainingTime / item.getmCurrentTime();
            int sub = (int) ratio * mCarryTime;
            item.setmCurrentTime(item.getmCurrentTime() - sub);
            mItemsList.set(i, item);
        }
        mCarryTime = 0;
    }

    // Next item
    public RoutineItem nextItem(RoutineItem currentItem) {
        //Sanity check
        if (mCurrentItemIndex + 1 >= mRoutineItemsNum) return null;

        // Time check
        if (currentItem.getmCurrentTime() == 0 && mCarryTime < 0) {
            distibuteCarryTime();
        } else if (currentItem.getmCurrentTime() > 0) {
            mCarryTime += currentItem.getmCurrentTime();
            currentItem.setmCurrentTime(0);
        }
        mItemsList.set(mCurrentItemIndex, currentItem);
        mCurrentItemIndex++;
        return mItemsList.get(mCurrentItemIndex);
    }

    // Previous item
    public RoutineItem prevItem(RoutineItem currentItem) {
        // Sanity check
        if (mCurrentItemIndex - 1 < 0) return null;

        // Save current item state, and return previous
        mItemsList.set(mCurrentItemIndex, currentItem);
        mCurrentItemIndex--;
        return mItemsList.get(mCurrentItemIndex);
    }

    // Reset routine
    public void resetRoutine() {
        mCarryTime = 0;
        mCurrentItemIndex = 0;

        // Clear items
        for (int i = 0; i < mItemsList.size(); i++) {
            mItemsList.get(i).resetItem();
        }

    }

    // Finish routine
    public void finishRoutine() {
        mCarryTime = 0;
        mCurrentItemIndex = 0;
        mTimesUsed++;

        for (int i = 0; i < mItemsList.size(); i++) {
            mItemsList.get(i).averageItemTime();
            mItemsList.get(i).resetItem();
        }
    }



    // Getters and Setters

    public RoutineItem getCurrentItem() {
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
        for (int i = 0; i < itemsList.size(); i++) {
            routineLen += itemsList.get(i).getmTime();
        }

        mLength = routineLen;
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

    public void setmCurrentItemIndex(int mCurrentItemIndex) {
        this.mCurrentItemIndex = mCurrentItemIndex;
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
}

package com.sxnwlfkk.dailyrituals;

/**
 * Created by cs on 2017.01.17..
 */

public class RitualItem {

    private String item_name;
    private int time;
    private int current_time;

    public void resetItem () {
        this.current_time = this.time;
    }

    public int getCurrent_time() {
        return current_time;
    }

    public void setCurrent_time(int current_time) {
        this.current_time = current_time;
    }

    public void setName (String name) {
        this.item_name = name;
    }

    public String getName () {
        return item_name;
    }

    public void setTime (int seconds) {
        this.time = seconds;
    }

    public int getTime () {
        return time;
    }
}

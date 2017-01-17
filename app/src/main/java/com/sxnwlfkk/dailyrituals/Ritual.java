package com.sxnwlfkk.dailyrituals;

/**
 * Created by cs on 2017.01.17..
 */

public class Ritual {

    private int carry_time;
    private String ritual_name;
    private int ritual_item_num;
    private int current_item;

    public void distribute_carry_loss () {

    }

    public String getRitual_name() {
        return ritual_name;
    }

    public void setRitual_name(String ritual_name) {
        this.ritual_name = ritual_name;
    }

    public int getRitual_item_num() {
        return ritual_item_num;
    }

    public void setRitual_item_num(int ritual_item_num) {
        this.ritual_item_num = ritual_item_num;
    }

    public int getCurrent_item() {
        return current_item;
    }

    public void setCurrent_item(int current_item) {
        this.current_item = current_item;
    }

    public int getCarry_time() {
        return carry_time;
    }

    public void setCarry_time(int carry_time) {
        this.carry_time = carry_time;
    }

}

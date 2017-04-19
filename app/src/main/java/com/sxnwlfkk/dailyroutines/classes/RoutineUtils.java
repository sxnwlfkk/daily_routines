package com.sxnwlfkk.dailyroutines.classes;

/**
 * Created by cs on 2017.04.17..
 */

public class RoutineUtils {

    public static String formatTimeString(int timeInSeconds) {
        if (timeInSeconds == 0) return "0 s";
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;

        String returnString = "";

        if (hours > 0) {
            returnString += hours + " h ";
        }
        if (minutes > 0) {
            returnString += minutes + " m ";
        }
        if (seconds > 0) {
            returnString += seconds + " s";
        }
        return returnString.trim();
    }

    // Calculates ideal starting time for routine. With the 60 division, we take off the
    // seconds and guarantees that it will be a little early.
    public static int calculateIdealStartTime(int endTime, int routineLength) {
        return ((endTime - routineLength) / 60) * 60;
    }
}

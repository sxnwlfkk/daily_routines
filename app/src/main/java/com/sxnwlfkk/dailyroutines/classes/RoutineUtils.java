package com.sxnwlfkk.dailyroutines.classes;

/**
 * Created by cs on 2017.04.17..
 */

public class RoutineUtils {
    public static final int AVERAGE_BIGGER = 1;
    public static final int AVERAGE_SMALLER = -1;
    public static final int AVERAGE_NIL_OR_EQ = 0;

    // Return a "HH h MM m SS s" formatted timestring. Used for durations
    public static String formatLengthString(int timeInSeconds) {
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

    // Returns a "HH:MM" formatted timestring (24h). Used for clock times
    public static String formatClockTimeString(int timeInSeconds) {
        if (timeInSeconds == 0) return "00:00";
        int hours = timeInSeconds / 3600;
        String rString = "";

        if (hours < 10)
            rString += "0" + hours;
        else
            rString += hours;
        rString += ":";
        int minutes = (timeInSeconds % 3600) / 60;
        if (minutes < 10)
            rString += "0" + minutes;
        else
            rString += minutes;
        return rString;
    }

    // Calculates ideal starting time for routine. With the 60 division, we take off the
    // seconds and guarantees that it will be a little early.
    public static int calculateIdealStartTime(int endTime, int routineLength) {
        return ((endTime - routineLength) / 60) * 60;
    }

    // Decides which color should the adapter use for average backgrounds
    public static int decideAvgColor(int length, int avg) {
        if (avg == 0 && length == avg) return AVERAGE_NIL_OR_EQ;
        else if (avg < length) return AVERAGE_SMALLER;
        else if (avg > length) return AVERAGE_BIGGER;
        return AVERAGE_NIL_OR_EQ;
    }
}

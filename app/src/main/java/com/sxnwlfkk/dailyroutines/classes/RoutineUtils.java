package com.sxnwlfkk.dailyroutines.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.sxnwlfkk.dailyroutines.views.clock.ClockService;

import java.util.Calendar;

/**
 * Created by cs on 2017.04.17..
 */

public class RoutineUtils {
    public static final int AVERAGE_BIGGER = 1;
    public static final int AVERAGE_SMALLER = -1;
    public static final int AVERAGE_NIL_OR_EQ = 0;

    // Return a "HH h MM m SS s" formatted timestring. Used for durations
    public static String formatLengthString(int timeInSeconds) {
        boolean isNegative = timeInSeconds < 0;
        if (isNegative) timeInSeconds *= -1;

        if (timeInSeconds == 0) return "0 s";
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;

        String returnString = "";
        if (isNegative) returnString = "-";

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

    // Only positive numbers
    public static String formatCountdownTimeString (int timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;
        String hour = "";
        String min = "";
        String sec = "";
        if (hours >= 1 && hours < 10) hour += ("0" + hours) + ":";
        if (hours >= 1 && hours > 9) hour += String.valueOf(hours) + ":";
        if (minutes < 10) min = "0";
        min += minutes;
        if (seconds < 10) sec = "0";
        sec += seconds;
        return hour + min + ":" + sec;
    }

    // Calculates ideal starting time for routine.
    // Arguments in seconds
    public static int calculateIdealStartTime(int endTime, int routineLength) {
        int startTime = (endTime - routineLength);
        if (startTime < 0) {
            startTime = (24*60*60) + startTime;
        }
        return startTime;
    }

    // Decides which color should the adapter use for average backgrounds
    public static int decideAvgColor(long length, double avg) {
        if (avg == 0 && length == avg) return AVERAGE_NIL_OR_EQ;
        else if (avg < length) return AVERAGE_SMALLER;
        else if (avg > length) return AVERAGE_BIGGER;
        return AVERAGE_NIL_OR_EQ;
    }

    // Converts seconds to msec-s
    public static long secToMsec(int sec) {
        return (long) sec * 1000;
    }

    // Converts msec-s to seconds with truncation
    public static int msecToSec(long msec) {
        return (int) msec / 1000;
    }

    public static int getCurrentTimeInSec() {
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        int currTime = (hours * 3600) + (minutes * 60) + seconds;

        return currTime;
    }

    // Reads from shared preferences the id of the currently running routine
    public static long readCurrentRoutine(Context ctx) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        final long currentRoutine = mPrefs.getLong(ClockService.CLOCK_ROUTINE_IN_PROGRESS, -1);
        return currentRoutine;
    }

    // Parses weekdays from the day picker rrule
    public static boolean[] parseAlarmDay(String rrule) {
        boolean[] daysSet = {false, false, false, false, false, false, false};

        if (rrule == null || rrule.equals("")) {
            return daysSet;
        }

        String[] days = getDaysString(rrule);
        for (int j = 0; j < days.length; j++) {
            daysSet[parseDayToNumber(days[j])] = true;
        }
        return daysSet;
    }

    public static String getDaysInPretty(String rrule) {
        if (rrule == null || rrule.equals("")) {
            return "";
        }

        String[] days = getDaysString(rrule);

        String prettyString = "";
        for (int i = 0; i < days.length; i++) {
            prettyString += days[i];
            if (i < days.length - 1) prettyString += " ";
        }
        return prettyString;
    }

    private static String[] getDaysString(String rrule) {
        String[] options = rrule.split(";");
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.DAY_OF_WEEK);
        int firstDayOfWeek = c.getFirstDayOfWeek();
        Log.d("Utils", "The first day of the week is: " + firstDayOfWeek);
        Log.d("Utils", "Today is the " + h + "th day.");
        c.setFirstDayOfWeek(Calendar.MONDAY);
        firstDayOfWeek = c.getFirstDayOfWeek();
        Log.d("Utils", "The first day of the week is: " + firstDayOfWeek);
        h = c.get(Calendar.DAY_OF_WEEK);
        Log.d("Utils", "Today is the " + h + "th day.");
        for (int i = 0; i < options.length; i++) {
            String[] kvs = options[i].split("=");
            if (kvs[0].equals("BYDAY")) {
                String[] days = kvs[1].split(",");
                return days;
            }
        }
        return null;
    }

    private static int parseDayToNumber(String day) {
        switch (day) {
            case "MO":
                return 1;
            case "SU":
                return 0;
            case "TU":
                return 2;
            case "WE":
                return 3;
            case "TH":
                return 4;
            case "FR":
                return 5;
            case "SA":
                return 6;
            default:
                return -1;
        }
    }

    public static Spanned getOptimalStartText(long endTime, long length, String rrule) {

        return Html.fromHtml(RoutineUtils.formatClockTimeString(
                RoutineUtils.calculateIdealStartTime((int) endTime / 1000, (int) length / 1000))
                + "   <i>" + RoutineUtils.getDaysInPretty(rrule) + "</i>");
    }
}

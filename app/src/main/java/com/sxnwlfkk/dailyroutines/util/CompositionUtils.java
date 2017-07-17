package com.sxnwlfkk.dailyroutines.util;

import android.content.Context;
import android.database.Cursor;

import com.sxnwlfkk.dailyroutines.classes.CompositionDialogRoutine;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;

import java.util.ArrayList;

/**
 * Created by sxnwlfkk on 2017.07.17..
 */

public class CompositionUtils {

    public static final String DEPENDENCY_STRING = "deps";

    // This loads and selects suitable routines for the edit composition dialog
    public static ArrayList<CompositionDialogRoutine> loadRoutinesForDialog(Context c, long id) {
        String[] projection = {
                RoutineContract.RoutineEntry._ID,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
                RoutineContract.RoutineEntry.COLUMN_ROUTINE_EXTRA_TEXT,
        };

        Cursor cursor = c.getContentResolver().query(
                RoutineContract.RoutineEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        ArrayList<CompositionDialogRoutine> retArray = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                CompositionDialogRoutine cdr = new CompositionDialogRoutine();
                long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                int length = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH));
                String settings = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_EXTRA_TEXT));

                if (isComposable(id, itemId, settings)) {
                    cdr.setId(itemId);
                    cdr.setName(name);
                    cdr.setLength(length);
                    cdr.setSettings(settings);

                    retArray.add(cdr);
                }
                cursor.moveToNext();
            }
        }

        return retArray;
    }

    // Decides if current routine should be shown as composable in dialog window. It is composable
    // if it has no dependencies, or it's not depending on the edited routine. In every other case
    // it's not composable.
    private static boolean isComposable(long idEdited, long idInQuestion, String settings) {
        ArrayList<Long> deps = readDependencies(settings);
        if (idEdited == idInQuestion) return false;
        if (deps == null) return true;
        if (deps.get(0) == (long) -1) return false;
        if (deps.size() == 0) return true;

        for (Long dependency:
             deps) {
            if (dependency == idEdited) return false;
        }
        return true;
    }

    // Parses the routines from the settings string
    public static ArrayList<Long> readDependencies(String settingsString) {
        if (settingsString == null || settingsString.isEmpty()) return null;

        ArrayList<Long> retArray = new ArrayList<>();
        String[] settings = settingsString.split(";");
        for (int i = 0; i < settings.length; i++) {
            String[] kvs = settings[i].split("=");
            if (kvs[0].equals(DEPENDENCY_STRING)) {
                if (kvs.length <= 1) return null;
                String[] dependencies = kvs[1].split(",");
                if (dependencies.length == 0) return null;
                for (String dependency : dependencies) {
                    try {
                        retArray.add(Long.parseLong(dependency));
                    } catch (Exception e) {
                        e.printStackTrace();
                        retArray.clear();
                        retArray.add((long) -1);
                        return retArray;
                    }
                }
            }
        }
        return retArray;
    }

    // Writes routines from id list in appropriate coding
    public static String writeDependenciesString(ArrayList<Long> deps) {
        final String emptyString = "deps=;";
        if (deps == null) return emptyString;
        if (deps.size() == 0) return emptyString;
        String depString = DEPENDENCY_STRING + "=";
        for (int i = 0; i < deps.size(); i++) {
            depString += Long.toString(deps.get(i));
            if (i+1 < deps.size()) depString += ",";
        }
        depString += ";";
        return depString;
    }

    // Has to be on a background thread, called after main list is loaded
    // This will update a routine's length and optimal start time in the main list, if one of the
    // components are changed.
    public void refreshRoutines() {

    }

    // This will read recursively the routines and returns a list with the final items
    // Used on ProfileView and ClockView
    public ArrayList<RoutineItem> composeRoutine() {

        return null;
    }



}

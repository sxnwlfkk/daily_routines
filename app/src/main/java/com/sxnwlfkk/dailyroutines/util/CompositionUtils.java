package com.sxnwlfkk.dailyroutines.util;

import com.sxnwlfkk.dailyroutines.classes.RoutineItem;

import java.util.ArrayList;

/**
 * Created by sxnwlfkk on 2017.07.17..
 */

public class CompositionUtils {

    public static final String DEPENDENCY_STRING = "deps";

    // This loads and selects suitable routines for the edit composition dialog
    public ArrayList loadRoutinesForDialog(long id) {

        return null;
    }

    // This will select the routines, which doesn't have already as a dependency the current
    // routine.
    private ArrayList selectSuitableRoutines(long id) {

        return null;
    }


    // Parses the routines from the settings string
    public static ArrayList<Long> readDependencies(String settingsString) {
        if (settingsString != null) {
            ArrayList<Long> retArray = new ArrayList<>();
            String[] settings = settingsString.split(";");
            for (int i = 0; i < settings.length; i++) {
                String[] kvs = settings[i].split("=");
                if (kvs[0].equals(DEPENDENCY_STRING)) {
                    if (kvs.length > 1) {
                        String[] dependencies = kvs[1].split(",");
                        if (dependencies.length != 0) {
                            for (String dependency : dependencies) {
                                try {
                                    retArray.add(Long.parseLong(dependency));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                        } else return null;

                    } else return null;
                }
            }
            return retArray;
        }
        return null;
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

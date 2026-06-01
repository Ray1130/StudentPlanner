package com.example.planner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHelper {
    private static final String PREF_NAME = "planner_settings";
    private static final String KEY_AUTO_DELETE_OPTION = "auto_delete_option";

    public static int getAutoDeleteOption(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_AUTO_DELETE_OPTION, 0); // Default to 0 (Không tự động xóa)
    }

    public static void setAutoDeleteOption(Context context, int option) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_AUTO_DELETE_OPTION, option).apply();
    }
}

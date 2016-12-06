package com.frodo.travigator.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by durgesh on 5/10/16.
 */
public class PrefManager {
    private static SharedPreferences sharedPreferences;
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.Pref.NAME, Context.MODE_PRIVATE);
        }
    }

    public static void enableTTS(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.Pref.TTS, enable);
        editor.apply();
    }

    public static boolean isTTSEnabled() {
        return sharedPreferences.getBoolean(Constants.Pref.TTS, false);
    }
}

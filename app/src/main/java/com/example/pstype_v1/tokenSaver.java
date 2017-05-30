package com.example.pstype_v1;

/**
 * Created by Derelanin on 29.05.2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class tokenSaver {
    private final static String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    private final static String TOKEN_KEY = "TOKEN_KEY";
    private final static String NAME = "NAME";

    public static String getToken(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, "");
    }
    public static String getName(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(NAME, "");
    }

    public static void setToken(Context c, String token) {
        SharedPreferences prefs = c.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }
    public static void setName(Context c, String name) {
        SharedPreferences prefs = c.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NAME, name);
        editor.apply();
    }

    public static void clearToken (Context c){
        SharedPreferences prefs = c.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, "");
        editor.putString(NAME, "");
        editor.apply();
    }
}
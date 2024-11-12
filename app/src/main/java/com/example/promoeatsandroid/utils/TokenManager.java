package com.example.promoeatsandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "prefs";
    private static final String ACCESS_TOKEN = "access_token";
    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(ACCESS_TOKEN, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ACCESS_TOKEN);
        editor.apply();
    }
}


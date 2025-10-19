package com.example.echosos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {
    }

    private static final String NAME = "echosos_prefs";
    private static final String KEY_UID = "current_user_id";

    public static void setUserId(Context c, long id) {
        SharedPreferences sp = c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_UID, id).apply();
    }

    public static long getUserId(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE).getLong(KEY_UID, -1);
    }

    public static String getPin(Context ctx) {
        return ctx.getSharedPreferences("echosos_prefs", Context.MODE_PRIVATE)
                .getString("pin", "");
    }

    public static void setPin(Context ctx, String pin) {
        ctx.getSharedPreferences("echosos_prefs", Context.MODE_PRIVATE)
                .edit().putString("pin", pin == null ? "" : pin).apply();
    }

}
package com.example.echosos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {}

    private static final String NAME = "echosos_prefs";
    private static final String KEY_UID = "current_user_id";

    // === Phase 4: location interval ===
    private static final String KEY_LOC_INTERVAL_MS = "loc_interval_ms";

    /** Mặc định 10_000 ms nếu chưa set */
    public static long getLocIntervalMs(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LOC_INTERVAL_MS, 10_000L);
    }

    // === Phase 4: auto recording toggle ===
    private static final String KEY_AUTO_RECORD = "auto_record_enabled";

    public static boolean isAutoRecordEnabled(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_RECORD, false);
    }

    public static void setAutoRecordEnabled(Context c, boolean enabled) {
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_AUTO_RECORD, enabled).apply();
    }


    /** Guard tối thiểu 3_000 ms */
    public static void setLocIntervalMs(Context c, long v) {
        long safe = Math.max(3_000L, v);
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putLong(KEY_LOC_INTERVAL_MS, safe).apply();
    }

    public static void setUserId(Context c, long id) {
        SharedPreferences sp = c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_UID, id).apply();
    }

    public static long getUserId(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE).getLong(KEY_UID, -1);
    }

    public static String getPin(Context ctx) {
        return ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString("pin", "");
    }

    public static void setPin(Context ctx, String pin) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putString("pin", pin == null ? "" : pin).apply();
    }

    // (tuỳ chọn cho Phase 4 sau này)
    // private static final String KEY_AUTO_RECORD = "auto_record_enabled";
    // public static boolean isAutoRecordEnabled(Context c) { ... }
    // public static void setAutoRecordEnabled(Context c, boolean b) { ... }
}

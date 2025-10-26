package com.example.echosos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {}

    private static final String NAME = "echosos_prefs";
    private static final String KEY_UID = "current_user_id";
    private static final String KEY_SAFE_MODE = "safe_mode";
    private static final String KEY_LOC_INTERVAL_MS = "loc_interval_ms";
    private static final String KEY_LANG_CODE = "lang_code";
    private static final String KEY_SOS_TEMPLATE = "sos_template";

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

    public static boolean isSafeMode(Context ctx) {
        return ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SAFE_MODE, false);
    }

    public static void setSafeMode(Context ctx, boolean enabled) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SAFE_MODE, enabled)
                .apply();
    }

    public static String getLangCode(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANG_CODE, "vi");
    }

    public static void setLangCode(Context c, String code) {
        if (code == null || code.trim().isEmpty()) code = "vi";
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANG_CODE, code).apply();
    }

    public static String getSosTemplate(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_SOS_TEMPLATE, "");
    }

    public static void setSosTemplate(Context c, String template) {
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_SOS_TEMPLATE, template == null ? "" : template).apply();
    }

    public static void clearAll(Context c) {
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        try {
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(c)
                    .edit()
                    .clear()
                    .apply();
        } catch (Throwable ignore) { }
    }

}

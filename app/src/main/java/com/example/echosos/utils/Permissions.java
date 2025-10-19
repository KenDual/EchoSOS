package com.example.echosos.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public final class Permissions {
    private Permissions() {
    }

    public static final String[] LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static String[] notifications() {
        return Build.VERSION.SDK_INT >= 33
                ? new String[]{Manifest.permission.POST_NOTIFICATIONS}
                : new String[]{};
    }

    public static boolean hasAll(Context ctx, String[] perms) {
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(ctx, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String[] contacts() {
        return new String[]{android.Manifest.permission.READ_CONTACTS};
    }

    public static String[] sms() {
        return new String[]{Manifest.permission.SEND_SMS};
    }

    public static String[] callPhone() {
        return new String[]{Manifest.permission.CALL_PHONE};
    }

    public static String[] audio() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    public static String[] merge(String[]... groups) {
        int len = 0;
        for (String[] g : groups) len += g.length;
        String[] out = new String[len];
        int i = 0;
        for (String[] g : groups) {
            for (String p : g) out[i++] = p;
        }
        return out;
    }

}
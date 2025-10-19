package com.example.echosos.utils;


import android.text.TextUtils;
import android.util.Patterns;

public final class PhoneUtils {
    private PhoneUtils() {
    }

    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9+]", ""); // giữ số và dấu +
    }

    public static boolean isValid(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }
}
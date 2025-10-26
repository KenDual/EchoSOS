package com.example.echosos.utils;

import android.content.Context;
import android.text.TextUtils;

public final class SosMessageBuilder {
    private SosMessageBuilder() {}

    public static String build(Context ctx, LocationFetcher.Fix fix) {
        if (fix == null) {
            String tpl = Prefs.getSosTemplate(ctx);
            if (!TextUtils.isEmpty(tpl)) {
                return applyTemplate(tpl, "N/A", "N/A", "N/A", "Không rõ", "https://maps.google.com");
            }
            return "Tôi đang gặp nguy hiểm. Không lấy được vị trí. — Gửi từ EchoSOS";
        }

        String lat = String.valueOf(fix.lat);
        String lng = String.valueOf(fix.lng);
        String acc = String.valueOf((int) fix.acc);
        String address = (fix.address == null || fix.address.trim().isEmpty()) ? "Không rõ" : fix.address;
        String maps = "https://maps.google.com/?q=" + fix.lat + "," + fix.lng;

        // 1) Nếu user có cấu hình template -> dùng template
        String tpl = Prefs.getSosTemplate(ctx);
        if (!TextUtils.isEmpty(tpl)) {
            return applyTemplate(tpl, lat, lng, acc, address, maps);
        }

        // 2) Mặc định (giữ nguyên như cũ)
        return "Tôi đang gặp nguy hiểm.\nVị trí: "
                + lat + "," + lng + " (±" + acc + "m)"
                + "\nĐịa chỉ: " + address
                + "\nBản đồ: " + maps
                + "\n— Gửi từ EchoSOS";
    }

    // Placeholder: {lat} {lng} {acc} {address} {maps}
    private static String applyTemplate(String tpl, String lat, String lng, String acc, String address, String maps) {
        return tpl
                .replace("{lat}", lat)
                .replace("{lng}", lng)
                .replace("{acc}", acc)
                .replace("{address}", address)
                .replace("{maps}", maps);
    }
}

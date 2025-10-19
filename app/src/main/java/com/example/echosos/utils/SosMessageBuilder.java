package com.example.echosos.utils;

import android.content.Context;

public final class SosMessageBuilder {
    private SosMessageBuilder() {
    }

    public static String build(Context ctx, LocationFetcher.Fix fix) {
        if (fix == null)
            return "Tôi đang gặp nguy hiểm. Không lấy được vị trí. — Gửi từ EchoSOS";
        String maps = "https://maps.google.com/?q=" + fix.lat + "," + fix.lng;
        return "Tôi đang gặp nguy hiểm.\nVị trí: "
                + fix.lat + "," + fix.lng + " (±" + (int) fix.acc + "m)"
                + "\nĐịa chỉ: " + fix.address
                + "\nBản đồ: " + maps
                + "\n— Gửi từ EchoSOS";
    }
}

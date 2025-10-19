package com.example.echosos.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import java.util.List;

public final class SmsSender {
    private SmsSender() {
    }

    public interface Callback {
        void onEachResult(String phone, boolean ok);

        void onAllDone();
    }

    public static void sendBulk(Context ctx, List<String> phones, String msg, Callback cb) {
        if (phones == null || phones.isEmpty()) {
            if (cb != null) cb.onAllDone();
            return;
        }
        SmsManager sm = SmsManager.getDefault();
        for (String p : phones) {
            try {
                // Optional SENT intent (không cần receiver cố định để tối giản Phase 3)
                PendingIntent sent = PendingIntent.getBroadcast(
                        ctx, p.hashCode(), new Intent("ECHOSOS_SMS_SENT"),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                sm.sendTextMessage(p, null, msg, sent, null);
                if (cb != null) cb.onEachResult(p, true);
            } catch (Exception e) {
                if (cb != null) cb.onEachResult(p, false);
            }
        }
        if (cb != null) cb.onAllDone();
    }
}

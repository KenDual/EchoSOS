package com.example.echosos.ui.main;

import android.content.Context;
import android.content.Intent;

import com.example.echosos.services.location.LocationService;
import com.example.echosos.services.recording.RecorderService;
import com.example.echosos.utils.Prefs;

public final class SafeModeController {
    private SafeModeController() {}

    public static boolean isEnabled(Context ctx) {
        return Prefs.isSafeMode(ctx);
    }

    /** Bật Safe Mode: lưu cờ + dừng mọi service nhạy cảm */
    public static void enable(Context ctx) {
        if (Prefs.isSafeMode(ctx)) return;
        Prefs.setSafeMode(ctx, true);

        // Dừng các service đang chạy
        ctx.stopService(new Intent(ctx, LocationService.class));
        ctx.stopService(new Intent(ctx, RecorderService.class));

        // TODO: (tuỳ chọn) ghi 1 log vào SosHistory:
        // try {
        //    SosHistoryDao dao = new SosHistoryDao(ctx);
        //    SosEvent e = new SosEvent();
        //    e.setMessage("SAFE_MODE_ON");
        //    e.setTimestamp(System.currentTimeMillis());
        //    dao.insert(e);
        // } catch (Exception ignore) {}
    }

    /** Tắt Safe Mode: chỉ bỏ cờ, KHÔNG tự start lại service */
    public static void disable(Context ctx) {
        if (!Prefs.isSafeMode(ctx)) return;
        Prefs.setSafeMode(ctx, false);
        // Không làm gì thêm. Người dùng chủ động bật lại theo nhu cầu.
    }
}

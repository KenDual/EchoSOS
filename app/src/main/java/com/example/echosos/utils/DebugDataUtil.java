package com.example.echosos.utils;

import android.content.Context;

import com.example.echosos.data.dao.EmergencyContactDao;
import com.example.echosos.data.model.EmergencyContact;

public final class DebugDataUtil {
    private DebugDataUtil(){}

    /** Seed 5 liên hệ mẫu cho user hiện tại. Trả về số bản ghi chèn được. */
    public static int seedTestContacts(Context ctx) {
        long userId = Prefs.getUserId(ctx);
        if (userId <= 0) return 0;

        EmergencyContactDao dao = new EmergencyContactDao(ctx);
        int inserted = 0;

        inserted += insert(dao, userId, "Tram Anh",   "+84123456001", 1, "Sister");
        inserted += insert(dao, userId, "Chi Bao",     "+84123456002", 2, "Friend");
        inserted += insert(dao, userId, "Phuong Thao",   "+84123456003", 3, "Colleague");
        inserted += insert(dao, userId, "Mai Phu Hai",   "+84123456004", 4, "Neighbor");
        inserted += insert(dao, userId, "khanh Nhu", "112", 5, "Hotline");

        // đặt Primary cho liên hệ có priority=1 (Alice)
        try {
            long primaryId = dao.getByUser(userId).stream()
                    .filter(ec -> ec.getPriority() == 1)
                    .findFirst().map(EmergencyContact::getId).orElse(-1L);
            if (primaryId > 0) dao.setPrimary(userId, primaryId);
        } catch (Throwable ignored) {}

        return inserted;
    }

    private static int insert(EmergencyContactDao dao, long userId, String name, String phone, int prio, String relation) {
        try {
            EmergencyContact ec = new EmergencyContact(userId, name, phone, prio);
            ec.setRelation(relation);
            dao.insert(ec);
            return 1;
        } catch (Exception ignored) {
            return 0;
        }
    }

}

package com.example.echosos.data.dao;

import static com.example.echosos.data.local.DatabaseContract.SosHistory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.SosEvent;

import java.util.ArrayList;
import java.util.List;

public class SosHistoryDao {
    private final DatabaseHelper helper;

    public SosHistoryDao(Context ctx) {
        this.helper = new DatabaseHelper(ctx);
    }

    /** Insert 1 bản ghi SOS; trả về rowId. */
    public long insert(SosEvent s) {
        ContentValues cv = new ContentValues();
        cv.put(SosHistory.COL_USER_ID, s.getUserId());
        cv.put(SosHistory.COL_LAT, s.getLat());
        cv.put(SosHistory.COL_LNG, s.getLng());
        cv.put(SosHistory.COL_ACCURACY, s.getAccuracy());
        cv.put(SosHistory.COL_ADDRESS, s.getAddress());
        cv.put(SosHistory.COL_MESSAGE, s.getMessage());
        cv.put(SosHistory.COL_CREATED, s.getCreatedAt());
        cv.put(SosHistory.COL_SMS_SENT, s.isSmsSent() ? 1 : 0);
        cv.put(SosHistory.COL_CALL_MADE, s.isCallMade() ? 1 : 0);
        cv.put(SosHistory.COL_AUDIO_URL, s.getAudioUrl());
        return helper.getWritableDatabase().insertOrThrow(SosHistory.TBL, null, cv);
    }

    /** Đánh dấu đã gửi SMS. */
    public int markSmsSent(long id, boolean sent) {
        ContentValues cv = new ContentValues();
        cv.put(SosHistory.COL_SMS_SENT, sent ? 1 : 0);
        return helper.getWritableDatabase().update(
                SosHistory.TBL, cv, SosHistory.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    /** Phase 4: cập nhật URL audio khi upload xong. */
    public int updateAudioUrl(long id, String url) {
        ContentValues cv = new ContentValues();
        cv.put(SosHistory.COL_AUDIO_URL, url);
        return helper.getWritableDatabase().update(
                SosHistory.TBL, cv, SosHistory.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    /** Phase 4: đánh dấu đã thực hiện cuộc gọi khẩn cấp. */
    public int markCallMade(long id) {
        ContentValues cv = new ContentValues();
        cv.put(SosHistory.COL_CALL_MADE, 1);
        return helper.getWritableDatabase().update(
                SosHistory.TBL, cv, SosHistory.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    /** Lấy danh sách gần nhất của 1 user. */
    public List<SosEvent> recentForUser(long userId, int limit) {
        List<SosEvent> out = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                SosHistory.TBL,
                null,
                SosHistory.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                SosHistory.COL_CREATED + " DESC",
                String.valueOf(Math.max(1, limit))
        )) {
            while (c.moveToNext()) out.add(map(c));
        }
        return out;
    }

    /** Lấy 1 bản ghi theo id (tiện dùng sau khi insert). */
    public SosEvent findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(
                SosHistory.TBL, null,
                SosHistory.COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, "1"
        )) {
            if (c.moveToFirst()) return map(c);
        }
        return null;
    }

    private static SosEvent map(Cursor c) {
        SosEvent s = new SosEvent();
        s.setId(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_ID)));
        s.setUserId(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_USER_ID)));
        s.setLat(getDoubleSafe(c, SosHistory.COL_LAT));
        s.setLng(getDoubleSafe(c, SosHistory.COL_LNG));
        s.setAccuracy(getDoubleSafe(c, SosHistory.COL_ACCURACY));
        s.setAddress(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_ADDRESS)));
        s.setMessage(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_MESSAGE)));
        s.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_CREATED)));
        s.setSmsSent(c.getInt(c.getColumnIndexOrThrow(SosHistory.COL_SMS_SENT)) == 1);
        s.setCallMade(c.getInt(c.getColumnIndexOrThrow(SosHistory.COL_CALL_MADE)) == 1);
        s.setAudioUrl(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_AUDIO_URL)));
        return s;
    }

    private static double getDoubleSafe(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx >= 0 && !c.isNull(idx)) return c.getDouble(idx);
        return 0d;
    }
}

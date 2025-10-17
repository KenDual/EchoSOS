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

    public int markSmsSent(long id, boolean sent) {
        ContentValues cv = new ContentValues();
        cv.put(SosHistory.COL_SMS_SENT, sent ? 1 : 0);
        return helper.getWritableDatabase().update(SosHistory.TBL, cv,
                SosHistory.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<SosEvent> recentForUser(long userId, int limit) {
        List<SosEvent> out = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(SosHistory.TBL, null,
                SosHistory.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                SosHistory.COL_CREATED + " DESC", String.valueOf(limit))) {
            while (c.moveToNext()) out.add(map(c));
        }
        return out;
    }

    private static SosEvent map(Cursor c) {
        SosEvent s = new SosEvent();
        s.setId(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_ID)));
        s.setUserId(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_USER_ID)));
        s.setLat(c.getDouble(c.getColumnIndexOrThrow(SosHistory.COL_LAT)));
        s.setLng(c.getDouble(c.getColumnIndexOrThrow(SosHistory.COL_LNG)));
        s.setAccuracy(c.getDouble(c.getColumnIndexOrThrow(SosHistory.COL_ACCURACY)));
        s.setAddress(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_ADDRESS)));
        s.setMessage(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_MESSAGE)));
        s.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(SosHistory.COL_CREATED)));
        s.setSmsSent(c.getInt(c.getColumnIndexOrThrow(SosHistory.COL_SMS_SENT)) == 1);
        s.setCallMade(c.getInt(c.getColumnIndexOrThrow(SosHistory.COL_CALL_MADE)) == 1);
        s.setAudioUrl(c.getString(c.getColumnIndexOrThrow(SosHistory.COL_AUDIO_URL)));
        return s;
    }
}
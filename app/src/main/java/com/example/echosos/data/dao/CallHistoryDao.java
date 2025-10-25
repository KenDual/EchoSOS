package com.example.echosos.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.echosos.data.local.DatabaseContract;
import com.example.echosos.data.model.CallLog;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryDao {
    private final SQLiteDatabase db;
    public CallHistoryDao(SQLiteDatabase db) { this.db = db; }

    public long insert(CallLog log) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.CallHistory.COL_USER_ID, log.getUserId());
        cv.put(DatabaseContract.CallHistory.COL_PHONE, log.getPhone());
        cv.put(DatabaseContract.CallHistory.COL_LABEL, log.getLabel());
        cv.put(DatabaseContract.CallHistory.COL_DURATION, log.getDurationSec());
        cv.put(DatabaseContract.CallHistory.COL_CREATED, log.getCreatedAt());
        return db.insert(DatabaseContract.CallHistory.TBL, null, cv);
    }

    public List<CallLog> recentForUser(long userId, int limit) {
        List<CallLog> out = new ArrayList<>();
        try (Cursor c = db.query(
                DatabaseContract.CallHistory.TBL,
                null,
                DatabaseContract.CallHistory.COL_USER_ID + "=?",
                new String[]{ String.valueOf(userId) },
                null, null,
                DatabaseContract.CallHistory.COL_CREATED + " DESC",
                String.valueOf(Math.max(1, limit))
        )) {
            int idCol       = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_ID);
            int userIdCol   = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_USER_ID);
            int phoneCol    = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_PHONE);
            int labelCol    = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_LABEL);
            int createdCol  = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_CREATED);
            int durationCol = c.getColumnIndexOrThrow(DatabaseContract.CallHistory.COL_DURATION);

            while (c.moveToNext()) {
                CallLog x = new CallLog();
                x.setId(c.getLong(idCol));
                x.setUserId(c.getLong(userIdCol));
                x.setPhone(c.getString(phoneCol));
                x.setLabel(c.isNull(labelCol) ? null : c.getString(labelCol));
                x.setCreatedAt(c.getLong(createdCol));
                x.setDurationSec(c.getInt(durationCol));
                out.add(x);
            }
        }
        return out;
    }
}

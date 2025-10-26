package com.example.echosos.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.echosos.data.local.DatabaseContract;
import com.example.echosos.data.model.RecordingChunk;

import java.util.ArrayList;
import java.util.List;

public class RecordingChunkDao {
    private final SQLiteDatabase db;
    public RecordingChunkDao(SQLiteDatabase db) { this.db = db; }

    public long enqueue(RecordingChunk ch) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecordingChunks.COL_USER_ID, ch.getUserId());
        cv.put(DatabaseContract.RecordingChunks.COL_EVENT_ID, ch.getEventId());
        cv.put(DatabaseContract.RecordingChunks.COL_PATH, ch.getLocalPath());
        cv.put(DatabaseContract.RecordingChunks.COL_STATUS, ch.getStatus()); // "queued"
        cv.put(DatabaseContract.RecordingChunks.COL_CREATED, ch.getCreatedAt());
        return db.insert(DatabaseContract.RecordingChunks.TBL, null, cv);
    }

    public int markUploaded(long id, String url) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecordingChunks.COL_STATUS, "uploaded");
        cv.put(DatabaseContract.RecordingChunks.COL_URL, url);
        return db.update(
                DatabaseContract.RecordingChunks.TBL,
                cv,
                DatabaseContract.RecordingChunks.COL_ID + "=?",
                new String[]{ String.valueOf(id) }
        );
    }

    public int markFailed(long id) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.RecordingChunks.COL_STATUS, "failed");
        return db.update(
                DatabaseContract.RecordingChunks.TBL,
                cv,
                DatabaseContract.RecordingChunks.COL_ID + "=?",
                new String[]{ String.valueOf(id) }
        );
    }

    public List<RecordingChunk> pending(int limit) {
        List<RecordingChunk> out = new ArrayList<>();
        try (Cursor c = db.query(
                DatabaseContract.RecordingChunks.TBL,
                null,
                DatabaseContract.RecordingChunks.COL_STATUS + "=?",
                new String[]{ "queued" },
                null, null,
                DatabaseContract.RecordingChunks.COL_CREATED + " ASC",
                String.valueOf(Math.max(1, limit))
        )) {
            int idCol      = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_ID);
            int userIdCol  = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_USER_ID);
            int eventIdCol = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_EVENT_ID);
            int pathCol    = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_PATH);
            int urlCol     = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_URL);
            int statusCol  = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_STATUS);
            int createdCol = c.getColumnIndexOrThrow(DatabaseContract.RecordingChunks.COL_CREATED);

            while (c.moveToNext()) {
                RecordingChunk x = new RecordingChunk();
                x.setId(c.getLong(idCol));
                x.setUserId(c.getLong(userIdCol));
                x.setEventId(c.getLong(eventIdCol));
                x.setLocalPath(c.getString(pathCol));
                x.setRemoteUrl(c.isNull(urlCol) ? null : c.getString(urlCol));
                x.setStatus(c.getString(statusCol));
                x.setCreatedAt(c.getLong(createdCol));
                out.add(x);
            }
        }
        return out;
    }

    public List<RecordingChunk> getPending() {
        List<RecordingChunk> list = new ArrayList<>();
        // Điều chỉnh tên bảng/cột nếu bạn dùng DatabaseContract khác
        Cursor c = db.rawQuery(
                "SELECT id, user_id, event_id, local_path, status, created_at " +
                        "FROM recording_chunks WHERE status=? ORDER BY created_at ASC",
                new String[]{"queued"}
        );
        try {
            while (c.moveToNext()) {
                RecordingChunk rc = new RecordingChunk();
                rc.setId(c.getLong(0));
                rc.setUserId(c.getLong(1));
                rc.setEventId(c.getLong(2));
                rc.setLocalPath(c.getString(3));
                rc.setStatus(c.getString(4));
                rc.setCreatedAt(c.getLong(5));
                list.add(rc);
            }
        } finally {
            c.close();
        }
        return list;
    }
}

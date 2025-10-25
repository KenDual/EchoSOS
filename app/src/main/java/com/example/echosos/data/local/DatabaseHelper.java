package com.example.echosos.data.local;

import static com.example.echosos.data.local.DatabaseContract.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        // Users
        db.execSQL(
                "CREATE TABLE " + Users.TBL + " (" +
                        Users.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Users.COL_NAME + " TEXT NOT NULL," +
                        Users.COL_PHONE + " TEXT NOT NULL UNIQUE," +
                        Users.COL_EMAIL + " TEXT," +
                        Users.COL_ADDRESS + " TEXT," +
                        Users.COL_PIN + " TEXT," +
                        Users.COL_CREATED + " INTEGER NOT NULL," +
                        Users.COL_UPDATED + " INTEGER NOT NULL" +
                        ")"
        );

        // Emergency Contacts
        db.execSQL(
                "CREATE TABLE " + EmergencyContacts.TBL + " (" +
                        EmergencyContacts.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        EmergencyContacts.COL_USER_ID + " INTEGER NOT NULL," +
                        EmergencyContacts.COL_NAME + " TEXT NOT NULL," +
                        EmergencyContacts.COL_PHONE + " TEXT NOT NULL," +
                        EmergencyContacts.COL_RELATION + " TEXT," +
                        EmergencyContacts.COL_PRIORITY + " INTEGER DEFAULT 0," +
                        EmergencyContacts.COL_CREATED + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + EmergencyContacts.COL_USER_ID + ") REFERENCES " +
                        Users.TBL + "(" + Users.COL_ID + ") ON DELETE CASCADE" +
                        ")"
        );

        // SOS History (đÃ bao gồm cột mới Phase 4)
        db.execSQL(
                "CREATE TABLE " + SosHistory.TBL + " (" +
                        SosHistory.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SosHistory.COL_USER_ID + " INTEGER NOT NULL," +
                        SosHistory.COL_LAT + " REAL," +
                        SosHistory.COL_LNG + " REAL," +
                        SosHistory.COL_ACCURACY + " REAL," +
                        SosHistory.COL_ADDRESS + " TEXT," +
                        SosHistory.COL_MESSAGE + " TEXT," +
                        SosHistory.COL_CREATED + " INTEGER NOT NULL," +
                        SosHistory.COL_SMS_SENT + " INTEGER DEFAULT 0," +
                        SosHistory.COL_CALL_MADE + " INTEGER DEFAULT 0," +
                        SosHistory.COL_AUDIO_URL + " TEXT," +
                        "FOREIGN KEY(" + SosHistory.COL_USER_ID + ") REFERENCES " +
                        Users.TBL + "(" + Users.COL_ID + ") ON DELETE CASCADE" +
                        ")"
        );

        // === Phase 4: bảng mới ===

        // Call History
        db.execSQL(
                "CREATE TABLE " + CallHistory.TBL + " (" +
                        CallHistory.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        CallHistory.COL_USER_ID + " INTEGER," +
                        CallHistory.COL_PHONE + " TEXT NOT NULL," +
                        CallHistory.COL_LABEL + " TEXT," +
                        CallHistory.COL_DURATION + " INTEGER DEFAULT 0," +
                        CallHistory.COL_CREATED + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + CallHistory.COL_USER_ID + ") REFERENCES " +
                        Users.TBL + "(" + Users.COL_ID + ") ON DELETE SET NULL" +
                        ")"
        );

        // Recording Chunks
        db.execSQL(
                "CREATE TABLE " + RecordingChunks.TBL + " (" +
                        RecordingChunks.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        RecordingChunks.COL_USER_ID + " INTEGER," +
                        RecordingChunks.COL_EVENT_ID + " INTEGER," +
                        RecordingChunks.COL_PATH + " TEXT NOT NULL," +
                        RecordingChunks.COL_URL + " TEXT," +
                        RecordingChunks.COL_STATUS + " TEXT NOT NULL," +     // queued|uploaded|failed
                        RecordingChunks.COL_CREATED + " INTEGER NOT NULL," +
                        "FOREIGN KEY(" + RecordingChunks.COL_USER_ID + ") REFERENCES " +
                        Users.TBL + "(" + Users.COL_ID + ") ON DELETE SET NULL," +
                        "FOREIGN KEY(" + RecordingChunks.COL_EVENT_ID + ") REFERENCES " +
                        SosHistory.TBL + "(" + SosHistory.COL_ID + ") ON DELETE CASCADE" +
                        ")"
        );

        // Indexes
        db.execSQL("CREATE INDEX idx_contacts_user ON " + EmergencyContacts.TBL +
                " (" + EmergencyContacts.COL_USER_ID + "," + EmergencyContacts.COL_PRIORITY + ")");
        db.execSQL("CREATE INDEX idx_sos_user_created ON " + SosHistory.TBL +
                " (" + SosHistory.COL_USER_ID + "," + SosHistory.COL_CREATED + ")");
        db.execSQL("CREATE INDEX idx_call_created ON " + CallHistory.TBL +
                " (" + CallHistory.COL_CREATED + ")");
        db.execSQL("CREATE INDEX idx_chunks_status ON " + RecordingChunks.TBL +
                " (" + RecordingChunks.COL_STATUS + ")");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Migration nhẹ cho dev; giữ data cũ nếu có
        if (oldV < 2) {
            // Thêm cột mới cho SosHistory (nếu device cũ chưa có)
            try {
                db.execSQL("ALTER TABLE " + SosHistory.TBL +
                        " ADD COLUMN " + SosHistory.COL_AUDIO_URL + " TEXT;");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + SosHistory.TBL +
                        " ADD COLUMN " + SosHistory.COL_CALL_MADE + " INTEGER DEFAULT 0;");
            } catch (Exception ignored) {}

            // Tạo mới bảng Phase 4 nếu chưa có
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + CallHistory.TBL + " (" +
                            CallHistory.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            CallHistory.COL_USER_ID + " INTEGER," +
                            CallHistory.COL_PHONE + " TEXT NOT NULL," +
                            CallHistory.COL_LABEL + " TEXT," +
                            CallHistory.COL_DURATION + " INTEGER DEFAULT 0," +
                            CallHistory.COL_CREATED + " INTEGER NOT NULL," +
                            "FOREIGN KEY(" + CallHistory.COL_USER_ID + ") REFERENCES " +
                            Users.TBL + "(" + Users.COL_ID + ") ON DELETE SET NULL" +
                            ")"
            );
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + RecordingChunks.TBL + " (" +
                            RecordingChunks.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            RecordingChunks.COL_USER_ID + " INTEGER," +
                            RecordingChunks.COL_EVENT_ID + " INTEGER," +
                            RecordingChunks.COL_PATH + " TEXT NOT NULL," +
                            RecordingChunks.COL_URL + " TEXT," +
                            RecordingChunks.COL_STATUS + " TEXT NOT NULL," +
                            RecordingChunks.COL_CREATED + " INTEGER NOT NULL," +
                            "FOREIGN KEY(" + RecordingChunks.COL_USER_ID + ") REFERENCES " +
                            Users.TBL + "(" + Users.COL_ID + ") ON DELETE SET NULL," +
                            "FOREIGN KEY(" + RecordingChunks.COL_EVENT_ID + ") REFERENCES " +
                            SosHistory.TBL + "(" + SosHistory.COL_ID + ") ON DELETE CASCADE" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_call_created ON " + CallHistory.TBL +
                    " (" + CallHistory.COL_CREATED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_chunks_status ON " + RecordingChunks.TBL +
                    " (" + RecordingChunks.COL_STATUS + ")");
            return;
        }

        // Nếu cần thay đổi lớn về sau, có thể fallback:
        // db.execSQL("DROP TABLE IF EXISTS " + RecordingChunks.TBL);
        // db.execSQL("DROP TABLE IF EXISTS " + CallHistory.TBL);
        // db.execSQL("DROP TABLE IF EXISTS " + SosHistory.TBL);
        // db.execSQL("DROP TABLE IF EXISTS " + EmergencyContacts.TBL);
        // db.execSQL("DROP TABLE IF EXISTS " + Users.TBL);
        // onCreate(db);
    }
}

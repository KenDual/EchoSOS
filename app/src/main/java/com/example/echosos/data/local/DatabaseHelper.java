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

        db.execSQL("CREATE INDEX idx_contacts_user ON " + EmergencyContacts.TBL +
                " (" + EmergencyContacts.COL_USER_ID + "," + EmergencyContacts.COL_PRIORITY + ")");
        db.execSQL("CREATE INDEX idx_sos_user_created ON " + SosHistory.TBL +
                " (" + SosHistory.COL_USER_ID + "," + SosHistory.COL_CREATED + ")");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // v1 -> v2…: viết migration thật ở đây. Tạm thời drop-create (dev).
        db.execSQL("DROP TABLE IF EXISTS " + SosHistory.TBL);
        db.execSQL("DROP TABLE IF EXISTS " + EmergencyContacts.TBL);
        db.execSQL("DROP TABLE IF EXISTS " + Users.TBL);
        onCreate(db);
    }
}

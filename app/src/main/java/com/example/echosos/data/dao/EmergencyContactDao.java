package com.example.echosos.data.dao;

import static com.example.echosos.data.local.DatabaseContract.EmergencyContacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactDao {
    private final DatabaseHelper helper;

    public EmergencyContactDao(Context ctx) {
        this.helper = new DatabaseHelper(ctx);
    }

    public long insert(EmergencyContact ec) {
        ContentValues cv = new ContentValues();
        cv.put(EmergencyContacts.COL_USER_ID, ec.getUserId());
        cv.put(EmergencyContacts.COL_NAME, ec.getName());
        cv.put(EmergencyContacts.COL_PHONE, ec.getPhone());
        cv.put(EmergencyContacts.COL_RELATION, ec.getRelation());
        cv.put(EmergencyContacts.COL_PRIORITY, ec.getPriority());
        cv.put(EmergencyContacts.COL_CREATED, ec.getCreatedAt());
        return helper.getWritableDatabase().insertOrThrow(EmergencyContacts.TBL, null, cv);
    }

    public int update(EmergencyContact ec) {
        ContentValues cv = new ContentValues();
        cv.put(EmergencyContacts.COL_NAME, ec.getName());
        cv.put(EmergencyContacts.COL_PHONE, ec.getPhone());
        cv.put(EmergencyContacts.COL_RELATION, ec.getRelation());
        cv.put(EmergencyContacts.COL_PRIORITY, ec.getPriority());
        return helper.getWritableDatabase().update(EmergencyContacts.TBL, cv,
                EmergencyContacts.COL_ID + "=?", new String[]{String.valueOf(ec.getId())});
    }

    public int delete(long id) {
        return helper.getWritableDatabase().delete(EmergencyContacts.TBL,
                EmergencyContacts.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<EmergencyContact> getByUser(long userId) {
        List<EmergencyContact> out = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(EmergencyContacts.TBL, null,
                EmergencyContacts.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                EmergencyContacts.COL_PRIORITY + " DESC," + EmergencyContacts.COL_CREATED + " ASC")) {
            while (c.moveToNext()) out.add(map(c));
        }
        return out;
    }

    public EmergencyContact getPrimary(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(EmergencyContacts.TBL, null,
                EmergencyContacts.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                EmergencyContacts.COL_PRIORITY + " DESC", "1")) {
            if (c.moveToFirst()) return map(c);
        }
        return null;
    }

    private static EmergencyContact map(Cursor c) {
        EmergencyContact e = new EmergencyContact();
        e.setId(c.getLong(c.getColumnIndexOrThrow(EmergencyContacts.COL_ID)));
        e.setUserId(c.getLong(c.getColumnIndexOrThrow(EmergencyContacts.COL_USER_ID)));
        e.setName(c.getString(c.getColumnIndexOrThrow(EmergencyContacts.COL_NAME)));
        e.setPhone(c.getString(c.getColumnIndexOrThrow(EmergencyContacts.COL_PHONE)));
        e.setRelation(c.getString(c.getColumnIndexOrThrow(EmergencyContacts.COL_RELATION)));
        e.setPriority(c.getInt(c.getColumnIndexOrThrow(EmergencyContacts.COL_PRIORITY)));
        e.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(EmergencyContacts.COL_CREATED)));
        return e;
    }

    public void setPrimary(long userId, long contactId){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv0 = new ContentValues(); cv0.put(EmergencyContacts.COL_PRIORITY, 0);
            db.update(EmergencyContacts.TBL, cv0, EmergencyContacts.COL_USER_ID+"=?", new String[]{String.valueOf(userId)});
            ContentValues cv1 = new ContentValues(); cv1.put(EmergencyContacts.COL_PRIORITY, 1);
            db.update(EmergencyContacts.TBL, cv1, EmergencyContacts.COL_ID+"=?", new String[]{String.valueOf(contactId)});
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

}
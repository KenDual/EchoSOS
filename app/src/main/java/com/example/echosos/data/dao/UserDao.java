package com.example.echosos.data.dao;

import static com.example.echosos.data.local.DatabaseContract.Users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final DatabaseHelper helper;

    public UserDao(Context ctx) {
        this.helper = new DatabaseHelper(ctx);
    }

    public long insert(User u) {
        long now = System.currentTimeMillis();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        ContentValues cv = new ContentValues();
        cv.put(Users.COL_NAME, u.getName());
        cv.put(Users.COL_PHONE, u.getPhone());
        cv.put(Users.COL_EMAIL, u.getEmail());
        cv.put(Users.COL_ADDRESS, u.getAddress());
        cv.put(Users.COL_PIN, u.getPinCode());
        cv.put(Users.COL_CREATED, u.getCreatedAt());
        cv.put(Users.COL_UPDATED, u.getUpdatedAt());
        return helper.getWritableDatabase().insertOrThrow(Users.TBL, null, cv);
    }

    public int update(User u) {
        u.setUpdatedAt(System.currentTimeMillis());
        ContentValues cv = new ContentValues();
        cv.put(Users.COL_NAME, u.getName());
        cv.put(Users.COL_PHONE, u.getPhone());
        cv.put(Users.COL_EMAIL, u.getEmail());
        cv.put(Users.COL_ADDRESS, u.getAddress());
        cv.put(Users.COL_PIN, u.getPinCode());
        cv.put(Users.COL_UPDATED, u.getUpdatedAt());
        return helper.getWritableDatabase().update(Users.TBL, cv, Users.COL_ID + "=?",
                new String[]{String.valueOf(u.getId())});
    }

    public int deleteById(long id) {
        return helper.getWritableDatabase().delete(Users.TBL, Users.COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public User findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(Users.TBL, null, Users.COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return map(c);
        }
        return null;
    }

    public User findByPhone(String phone) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(Users.TBL, null, Users.COL_PHONE + "=?",
                new String[]{phone}, null, null, null)) {
            if (c.moveToFirst()) return map(c);
        }
        return null;
    }

    public List<User> getAll() {
        List<User> out = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(Users.TBL, null, null, null, null, null, Users.COL_CREATED + " DESC")) {
            while (c.moveToNext()) out.add(map(c));
        }
        return out;
    }

    private static User map(Cursor c) {
        User u = new User();
        u.setId(c.getLong(c.getColumnIndexOrThrow(Users.COL_ID)));
        u.setName(c.getString(c.getColumnIndexOrThrow(Users.COL_NAME)));
        u.setPhone(c.getString(c.getColumnIndexOrThrow(Users.COL_PHONE)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(Users.COL_EMAIL)));
        u.setAddress(c.getString(c.getColumnIndexOrThrow(Users.COL_ADDRESS)));
        u.setPinCode(c.getString(c.getColumnIndexOrThrow(Users.COL_PIN)));
        u.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(Users.COL_CREATED)));
        u.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(Users.COL_UPDATED)));
        return u;
    }
}
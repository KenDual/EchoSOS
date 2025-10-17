package com.example.echosos.data.local;

public class DatabaseContract {
    private DatabaseContract(){}

    public static final String DB_NAME = "echosos.db";
    public static final int DB_VERSION = 1;

    public static final class Users {
        public static final String TBL = "users";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String COL_PHONE = "phone";
        public static final String COL_EMAIL = "email";
        public static final String COL_ADDRESS = "address";
        public static final String COL_PIN = "pin_code";
        public static final String COL_CREATED = "created_at";
        public static final String COL_UPDATED = "updated_at";
    }

    public static final class EmergencyContacts {
        public static final String TBL = "emergency_contacts";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_NAME = "name";
        public static final String COL_PHONE = "phone";
        public static final String COL_RELATION = "relation";
        public static final String COL_PRIORITY = "priority";
        public static final String COL_CREATED = "created_at";
    }

    public static final class SosHistory {
        public static final String TBL = "sos_history";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_LAT = "lat";
        public static final String COL_LNG = "lng";
        public static final String COL_ACCURACY = "accuracy";
        public static final String COL_ADDRESS = "address";
        public static final String COL_MESSAGE = "message";
        public static final String COL_CREATED = "created_at";
        public static final String COL_SMS_SENT = "sms_sent";
        public static final String COL_CALL_MADE = "call_made";
        public static final String COL_AUDIO_URL = "audio_url";
    }
}

package com.example.echosos.data.local;

public class DatabaseContract {
    private DatabaseContract(){}

    public static final String DB_NAME = "echosos.db";
    public static final int DB_VERSION = 2;

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

    public static final class CallHistory {
        public static final String TBL = "call_history";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_PHONE = "phone";
        public static final String COL_LABEL = "label";
        public static final String COL_CREATED = "created_at";
        public static final String COL_DURATION = "duration_sec";
    }

    public static final class RecordingChunks {
        public static final String TBL = "recording_chunks";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_EVENT_ID = "event_id";
        public static final String COL_PATH = "local_path";
        public static final String COL_URL = "remote_url";
        public static final String COL_STATUS = "status";
        public static final String COL_CREATED = "created_at";
    }

}

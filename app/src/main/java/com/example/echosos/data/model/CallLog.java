package com.example.echosos.data.model;

public class CallLog {
    private long id;
    private long userId;
    private String phone;
    private String label;
    private long createdAt;
    private int durationSec;

    public CallLog() { }

    public CallLog(long id, long userId, String phone, String label, long createdAt, int durationSec) {
        this.id = id;
        this.userId = userId;
        this.phone = phone;
        this.label = label;
        this.createdAt = createdAt;
        this.durationSec = durationSec;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }
}

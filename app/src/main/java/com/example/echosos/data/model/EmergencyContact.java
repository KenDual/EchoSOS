package com.example.echosos.data.model;

public class EmergencyContact {
    private long id;
    private long userId;
    private String name;
    private String phone;
    private String relation;
    private int priority;     // 0=normal, 1=primary...
    private long createdAt;

    public EmergencyContact() {
    }

    public EmergencyContact(long userId, String name, String phone, int priority) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.priority = priority;
        this.createdAt = System.currentTimeMillis();
    }

    // getters/setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String r) {
        this.relation = r;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int p) {
        this.priority = p;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long t) {
        this.createdAt = t;
    }
}

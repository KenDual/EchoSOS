package com.example.echosos.data.model;

public class User {
    private long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String pinCode;
    private long createdAt;
    private long updatedAt;

    public User() {
    }

    public User(String name, String phone) {
        this.name = name;
        this.phone = phone;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // getters/setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long t) {
        this.createdAt = t;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long t) {
        this.updatedAt = t;
    }
}

package com.example.echosos.data.model;

public class SosEvent {
    private long id;
    private long userId;
    private double lat;
    private double lng;
    private double accuracy;
    private String address;
    private String message;
    private long createdAt;
    private boolean smsSent;
    private boolean callMade;
    private String audioUrl;

    public SosEvent() {
    }

    public SosEvent(long userId, double lat, double lng, String msg) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.message = msg;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double a) {
        this.accuracy = a;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String a) {
        this.address = a;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        this.message = m;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long t) {
        this.createdAt = t;
    }

    public boolean isSmsSent() {
        return smsSent;
    }

    public void setSmsSent(boolean b) {
        this.smsSent = b;
    }

    public boolean isCallMade() {
        return callMade;
    }

    public void setCallMade(boolean b) {
        this.callMade = b;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String u) {
        this.audioUrl = u;
    }
}

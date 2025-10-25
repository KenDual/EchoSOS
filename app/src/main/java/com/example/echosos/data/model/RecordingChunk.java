package com.example.echosos.data.model;

public class RecordingChunk {
    private long id;
    private long userId;
    private long eventId;
    private String localPath;
    private String remoteUrl;
    private String status;
    private long createdAt;

    public RecordingChunk() { }

    public RecordingChunk(long userId, long id, long eventId, String localPath, String remoteUrl, String status, long createdAt) {
        this.userId = userId;
        this.id = id;
        this.eventId = eventId;
        this.localPath = localPath;
        this.remoteUrl = remoteUrl;
        this.status = status;
        this.createdAt = createdAt;
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

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

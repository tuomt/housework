package com.example.housework.data;

public class User {
    private final long id;
    private final String name;
    private final String email;
    private long groupId;
    private final String accessToken;

    public User(long id, String name, String email, long groupId, String accessToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.groupId = groupId;
        this.accessToken = accessToken;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public long getGroupId() {
        return this.groupId;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
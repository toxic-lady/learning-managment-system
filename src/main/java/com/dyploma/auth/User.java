package com.dyploma.auth;

public class User {
    private int userId;
    private String fullName;

    public User(int userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }
}



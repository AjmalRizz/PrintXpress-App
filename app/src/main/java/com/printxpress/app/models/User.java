package com.printxpress.app.models;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private Timestamp createdAt;

    // Required empty constructor for Firestore
    public User() {}

    public User(String uid, String name, String email, String phone, Timestamp createdAt) {
        this(uid, name, email, phone, "", createdAt);
    }

    public User(String uid, String name, String email, String phone, String profileImageUrl, Timestamp createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

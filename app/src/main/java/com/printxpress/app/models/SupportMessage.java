package com.printxpress.app.models;

import com.google.firebase.Timestamp;

public class SupportMessage {
    private String messageId;
    private String uid;
    private String userEmail;
    private String message;
    private Timestamp createdAt;

    public SupportMessage() {}

    public SupportMessage(String messageId, String uid, String userEmail, String message, Timestamp createdAt) {
        this.messageId = messageId;
        this.uid = uid;
        this.userEmail = userEmail;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

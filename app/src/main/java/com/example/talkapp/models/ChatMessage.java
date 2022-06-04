package com.example.talkapp.models;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    String text, name, photoUrl, messageId, userId;
    long timestamp;

    public ChatMessage() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ChatMessage(String text, String name, String photoUrl, String messageId, String userId, long timestamp) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.messageId = messageId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

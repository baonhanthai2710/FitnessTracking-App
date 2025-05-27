package com.example.fitnesstrackingapp;

public class ChatMessage {
    private final String content;
    private final boolean isUser;

    public ChatMessage(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}
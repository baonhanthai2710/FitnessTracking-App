package com.example.fitnesstrackingapp;


public class Event {
    private String id;
    private String title;
    private String time;
    private long timestamp;

    // Constructor mặc định cần cho Firebase
    public Event() {}

    public Event(String id, String title, String time, long timestamp) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
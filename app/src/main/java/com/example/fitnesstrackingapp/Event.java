package com.example.fitnesstrackingapp;


import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {
    private String id;
    private String title;
    private String description;
    private String location;
    private String type; // "event" hoặc "appointment"
    private long timestamp;

    public Event() {}

    public Event(String id, String title, String description, String location, String type, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    // Thêm getter/setter cho các trường mới
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
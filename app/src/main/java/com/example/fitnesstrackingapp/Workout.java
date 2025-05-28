package com.example.fitnesstrackingapp;

import java.util.Date;

public class Workout {
    private String id;
    private String type;
    private int duration;
    private int caloriesBurned;
    private String notes;
    private Date date;
    private long timestamp; // Thêm timestamp để dễ sắp xếp
    
    // Cần constructor trống cho Firebase Deserialization
    public Workout() {
    }
    
    public Workout(String type, int duration, int caloriesBurned, String notes, Date date) {
        this.type = type;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.notes = notes;
        this.date = date;
        this.timestamp = date.getTime();
    }
    
    // Getters và setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
        this.timestamp = date.getTime();
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.date = new Date(timestamp);
    }
}
package com.example.fitnesstrackingapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Bật chế độ offline persistence ngay khi App start
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

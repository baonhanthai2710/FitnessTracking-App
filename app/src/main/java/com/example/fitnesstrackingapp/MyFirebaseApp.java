package com.example.fitnesstrackingapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize App Check
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        
        // Cho development - sử dụng Debug provider
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
        
        // Cho production - sử dụng Play Integrity
        // firebaseAppCheck.installAppCheckProviderFactory(
        //         PlayIntegrityAppCheckProviderFactory.getInstance());
        
        // Bật chế độ offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

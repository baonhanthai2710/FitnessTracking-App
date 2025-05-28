package com.example.fitnesstrackingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.time.LocalDate;

public class StepCounterService extends Service implements SensorEventListener {
    
    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "StepCounterChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SharedPreferences prefs;
    
    private int stepsToday = 0;
    private int totalStepsFromSensor = 0;
    private int stepsOffset = 0;
    private String currentDate;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        
        // Initialize current date
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }
        
        // Load step data
        loadStepData();
        
        // Initialize step sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Register sensor listener
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Step counter sensor registered");
        } else {
            Log.e(TAG, "Step counter sensor not available");
        }
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        return START_STICKY; // Service will be restarted if killed
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        Log.d(TAG, "Step counter service destroyed");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalStepsFromSensor = (int) event.values[0];
            
            // Check if we need to reset the step counter for a new day
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String today = getCurrentDate();
                if (!today.equals(currentDate)) {
                    // A new day has started
                    stepsOffset = totalStepsFromSensor - stepsToday;
                    saveStepData(0, stepsOffset);
                    currentDate = today;
                    stepsToday = 0;
                    Log.d(TAG, "New day detected, resetting step counter");
                }
            }
            
            // Calculate today's steps
            stepsToday = totalStepsFromSensor - stepsOffset;
            
            // Save step data
            saveStepData(stepsToday, stepsOffset);
            
            // Update notification
            updateNotification();
            
            Log.d(TAG, "Steps today: " + stepsToday + ", Total: " + totalStepsFromSensor);
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    private void loadStepData() {
        stepsToday = prefs.getInt(Constants.STEPS_TODAY_KEY, 0);
        stepsOffset = prefs.getInt(Constants.STEPS_OFFSET_KEY, 0);
        String lastResetDate = prefs.getString(Constants.LAST_RESET_DATE_KEY, "");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String today = getCurrentDate();
            if (!lastResetDate.equals(today)) {
                // It's a new day, reset the step counter
                stepsToday = 0;
                saveStepData(stepsToday, stepsOffset);
                currentDate = today;
            }
        }
    }
    
    private void saveStepData(int steps, int offset) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.STEPS_TODAY_KEY, steps);
        editor.putInt(Constants.STEPS_OFFSET_KEY, offset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editor.putString(Constants.LAST_RESET_DATE_KEY, getCurrentDate());
        }
        editor.apply();
    }
    
    private String getCurrentDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().toString();
        }
        return "";
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your daily steps");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fitness Tracker")
                .setContentText("Đã đi " + stepsToday + " bước hôm nay")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
    
    private void updateNotification() {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }
} 
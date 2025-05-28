package com.example.fitnesstrackingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.LocalDate;

import static com.example.fitnesstrackingapp.Constants.PREFS_NAME;
import static com.example.fitnesstrackingapp.Constants.STEPS_TODAY_KEY;
import static com.example.fitnesstrackingapp.Constants.STEPS_OFFSET_KEY;
import static com.example.fitnesstrackingapp.Constants.LAST_RESET_DATE_KEY;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // Step Counter Variables
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isStepCounterSensorPresent = false;
    private int stepsToday = 0;
    private int totalStepsFromSensor = 0;
    private int stepsOffset = 0;
    private SharedPreferences prefs;
    private String currentDate;
    
    // Tabs & ViewPager
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabPagerAdapter adapter;
    
    // Progress bar
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "MainActivity onCreate");
        
        // Initialize progress bar
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize ViewPager and TabLayout
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Set up ViewPager adapter
        adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Dashboard");
                    break;
                case 1:
                    tab.setText("Quick Access");
                    break;
                case 2:
                    tab.setText("Events");
                    break;
                case 3:
                    tab.setText("Worklog");
                    break;
                case 4:
                    tab.setText("AI Chatbot");
                    break;
            }
        }).attach();
        
        // Check permissions first
        checkAndRequestPermissions();
        
        // Initialize step counter
        initializeStepCounter();
    }
    
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 
                        PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Activity recognition permission already granted");
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Activity recognition permission granted");
                Toast.makeText(this, "Permission granted for step counting", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Activity recognition permission denied");
                Toast.makeText(this, "Permission required for step counting", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void initializeStepCounter() {
        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }
        
        loadStepData(); // Load saved step data
        
        // Initialize step sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounterSensor != null) {
                isStepCounterSensorPresent = true;
                Log.d(TAG, "Step counter sensor found");
                
                // Start the step counter service
                startStepCounterService();
            } else {
                Log.e(TAG, "Step counter sensor not available on this device");
                Toast.makeText(this, "Step counter not supported on this device", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "SensorManager not available");
        }
        
        // Update UI immediately with saved data
        updateDashboardFragment();
    }
    
    private void startStepCounterService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.d(TAG, "Step counter service started");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isStepCounterSensorPresent && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Sensor listener registered");
        }
        
        // Reload step data in case it was updated by the service
        loadStepData();
        updateDashboardFragment();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isStepCounterSensorPresent && sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listener unregistered");
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalStepsFromSensor = (int) event.values[0];
            
            Log.d(TAG, "Sensor changed - Total steps: " + totalStepsFromSensor);
            
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
            
            // Calculate today's steps by subtracting the offset
            stepsToday = totalStepsFromSensor - stepsOffset;
            
            Log.d(TAG, "Steps today: " + stepsToday);
            
            // Update dashboard fragment if it's visible
            updateDashboardFragment();
            
            // Save the current step count
            saveStepData(stepsToday, stepsOffset);
        }
    }
    
    private void updateDashboardFragment() {
        // Get the current fragment from ViewPager2
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        
        // Also try to get dashboard fragment specifically (it's always at position 0)
        Fragment dashboardFragment = getSupportFragmentManager().findFragmentByTag("f0");
        
        if (dashboardFragment instanceof DashboardFragment) {
            ((DashboardFragment) dashboardFragment).updateStepCounter(stepsToday);
            ((DashboardFragment) dashboardFragment).updateTotalSteps(totalStepsFromSensor);
            Log.d(TAG, "Dashboard updated with steps: " + stepsToday);
        } else {
            // Alternative method to update dashboard
            runOnUiThread(() -> {
                // Try to find dashboard fragment in all fragments
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof DashboardFragment) {
                        ((DashboardFragment) fragment).updateStepCounter(stepsToday);
                        ((DashboardFragment) fragment).updateTotalSteps(totalStepsFromSensor);
                        Log.d(TAG, "Dashboard updated via alternative method");
                        break;
                    }
                }
            });
        }
    }
    
    private void loadStepData() {
        stepsToday = prefs.getInt(STEPS_TODAY_KEY, 0);
        stepsOffset = prefs.getInt(STEPS_OFFSET_KEY, 0);
        String lastResetDate = prefs.getString(LAST_RESET_DATE_KEY, "");
        
        Log.d(TAG, "Loaded step data - Today: " + stepsToday + ", Offset: " + stepsOffset);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String today = getCurrentDate();
            if (!lastResetDate.equals(today)) {
                // It's a new day, reset the step counter
                stepsToday = 0;
                saveStepData(stepsToday, stepsOffset);
                currentDate = today;
                Log.d(TAG, "New day detected during load, reset steps");
            }
        }
    }
    
    private void saveStepData(int steps, int offset) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(STEPS_TODAY_KEY, steps);
        editor.putInt(STEPS_OFFSET_KEY, offset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editor.putString(LAST_RESET_DATE_KEY, getCurrentDate());
        }
        editor.apply();
        Log.d(TAG, "Step data saved - Today: " + steps + ", Offset: " + offset);
    }
    
    private String getCurrentDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().toString();
        }
        return "";
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
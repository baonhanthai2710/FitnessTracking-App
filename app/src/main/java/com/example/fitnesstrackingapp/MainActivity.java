package com.example.fitnesstrackingapp;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.LocalDate;
import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

import static com.example.fitnesstrackingapp.Constants.PREFS_NAME;
import static com.example.fitnesstrackingapp.Constants.STEPS_TODAY_KEY;
import static com.example.fitnesstrackingapp.Constants.STEPS_OFFSET_KEY;
import static com.example.fitnesstrackingapp.Constants.LAST_RESET_DATE_KEY;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Step Counter Variables
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isStepCounterSensorPresent;
    private int stepsToday = 0;
    private int totalStepsFromSensor = 0;
    private int previousTotalStepsFromSensor = 0;
    private int stepsOffset = 0;
    private SharedPreferences prefs;
    private String currentDate;
    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String STEPS_TODAY_KEY = "steps_today";
    private static final String STEPS_OFFSET_KEY = "steps_offset";
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";
    
    // Tabs & ViewPager
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabPagerAdapter adapter;
    private DashboardFragment dashboardFragment;
    
    // Progress bar
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
            }
        }).attach();
        
        // Step counter initialization
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }
        
        loadStepData(); // Load saved step data
        
        // Initialize step sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isStepCounterSensorPresent = true;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isStepCounterSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isStepCounterSensorPresent) {
            sensorManager.unregisterListener(this);
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalStepsFromSensor = (int) event.values[0];
            
            // Check if we need to reset the step counter for a new day
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                String today = getCurrentDate();
                if (!today.equals(currentDate)) {
                    // A new day has started
                    stepsOffset = totalStepsFromSensor - stepsToday;
                    saveStepData(0, stepsOffset);
                    currentDate = today;
                    stepsToday = 0;
                }
            }
            
            // Calculate today's steps by subtracting the offset
            stepsToday = totalStepsFromSensor - stepsOffset;
            
            // Update dashboard fragment if it's visible
            updateDashboardFragment();
            
            // Save the current step count
            saveStepData(stepsToday, stepsOffset);
        }
    }
    
    private void updateDashboardFragment() {
        // Find the current dashboard fragment to update its UI
        int currentIndex = viewPager.getCurrentItem();
        if (currentIndex == 0) { // Dashboard is the first tab
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f0");
            if (fragment instanceof DashboardFragment) {
                ((DashboardFragment) fragment).updateStepCounter(stepsToday);
                ((DashboardFragment) fragment).updateTotalSteps(totalStepsFromSensor);
            }
        }
    }
    
    private void loadStepData() {
        stepsToday = prefs.getInt(STEPS_TODAY_KEY, 0);
        stepsOffset = prefs.getInt(STEPS_OFFSET_KEY, 0);
        String lastResetDate = prefs.getString(LAST_RESET_DATE_KEY, "");
        
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
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
        editor.putInt(STEPS_TODAY_KEY, steps);
        editor.putInt(STEPS_OFFSET_KEY, offset);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            editor.putString(LAST_RESET_DATE_KEY, getCurrentDate());
        }
        editor.apply();
    }
    
    private String getCurrentDate() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            return LocalDate.now().toString();
        }
        return "";
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
package com.example.fitnesstrackingapp;
import static android.content.ContentValues.TAG;
import static android.os.Build.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isStepCounterSensorPresent;
    private int stepsToday = 0;
    private int totalStepsFromSensor = 0;
    private int previousTotalStepsFromSensor = 0;
    private int stepsOffset = 0;
    private static final String PREFS_NAME = "StepCounterPrefs";
    public static final String KEY_STEPS_TODAY = "stepsToday";
    private static final String KEY_TOTAL_STEPS = "totalSteps";
    private static final String KEY_STEPS_OFFSET = "stepsOffset";
    public static final String KEY_SAVED_DATE = "savedDate";
    private String currentDate;
    private SharedPreferences prefs;
    public static final String KEY_STEP_HISTORY = "stepHistory";
    public static final String KEY_PREVIOUS_TOTAL_STEPS = "previousTotalSteps";
    private Button btnViewDashboard;
    private Sensor sensor;
    private int accuracy;
    private TextView tvStepCounter;  // Khai báo tvStepCounter
    private TextView tvTotalStepsSensor; // Khai báo tvTotalStepsSensor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStepCounter = findViewById(R.id.tv_step_counter);  // Ánh xạ tvStepCounter
        tvTotalStepsSensor = findViewById(R.id.tv_total_steps_sensor);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }

        loadStepData(); // Tải dữ liệu đã lưu

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isStepCounterSensorPresent = true;
            // Đăng ký listener trong onResume()
        } else {
            // Xử lý trường hợp không có cảm biến
        }

        btnViewDashboard = findViewById(R.id.btn_view_dashboard);
        btnViewDashboard.setVisibility(android.view.View.VISIBLE);
        btnViewDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        });
    }

    @RequiresApi(api = VERSION_CODES.O)
    private String getCurrentDate() {
        // ... (Hàm lấy ngày hiện tại)
        return LocalDate.now().toString(); // Ví dụ
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isStepCounterSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        checkDateAndResetStepsIfNeeded();
        updateUI(); // Thêm hàm này để cập nhật UI nếu cần
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStepCounterSensorPresent) {
            sensorManager.unregisterListener(this);
            saveStepData();
        }
    }
    private void loadStepData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_SAVED_DATE, "");
        String currentDate = null;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }
        Log.d(TAG, "loadStepData: START - savedDate=" + savedDate + ", currentDate=" + currentDate);

        if (savedDate.equals(currentDate)) {
            Log.d(TAG, "loadStepData: SAME DAY");
            previousTotalStepsFromSensor = prefs.getInt(KEY_PREVIOUS_TOTAL_STEPS, 0);
            stepsOffset = prefs.getInt(KEY_STEPS_OFFSET, 0);
            totalStepsFromSensor = prefs.getInt(KEY_TOTAL_STEPS, 0);
            stepsToday = prefs.getInt(KEY_STEPS_TODAY, 0);
            Log.d(TAG, "loadStepData: SAME DAY - previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsOffset=" + stepsOffset + ", totalStepsFromSensor=" + totalStepsFromSensor + ", stepsToday=" + stepsToday);
        } else {
            Log.d(TAG, "loadStepData: NEW DAY");
            previousTotalStepsFromSensor = prefs.getInt(KEY_PREVIOUS_TOTAL_STEPS, 0);
            stepsToday = 0;
            stepsOffset = 0;
            prefs.edit().putInt(KEY_STEPS_OFFSET, 0).putInt(KEY_STEPS_TODAY, 0).apply();
            Log.d(TAG, "loadStepData: NEW DAY - previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsOffset=" + stepsOffset + ", stepsToday=" + stepsToday);
        }
        Log.d(TAG, "loadStepData: END - previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsOffset=" + stepsOffset + ", totalStepsFromSensor=" + totalStepsFromSensor + ", stepsToday=" + stepsToday);
    }

    private void checkDateAndResetStepsIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_SAVED_DATE, "");
        String currentDate = null;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            currentDate = getCurrentDate();
        }
        Log.d(TAG, "checkDateAndResetStepsIfNeeded: START - savedDate=" + savedDate + ", currentDate=" + currentDate);

        if (!savedDate.equals(currentDate)) {
            Log.d(TAG, "checkDateAndResetStepsIfNeeded: DATE CHANGED");
            previousTotalStepsFromSensor = totalStepsFromSensor;
            stepsToday = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_SAVED_DATE, currentDate);
            editor.putInt(KEY_PREVIOUS_TOTAL_STEPS, previousTotalStepsFromSensor);
            editor.putInt(KEY_STEPS_TODAY, 0);
            editor.apply();
            stepsOffset = previousTotalStepsFromSensor; // Hoặc 0, tùy logic
            prefs.edit().putInt(KEY_STEPS_OFFSET, stepsOffset).apply();
            Log.d(TAG, "checkDateAndResetStepsIfNeeded: DATE CHANGED - previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsToday=" + stepsToday + ", stepsOffset=" + stepsOffset);
        } else {
            Log.d(TAG, "checkDateAndResetStepsIfNeeded: SAME DATE");
        }
        Log.d(TAG, "checkDateAndResetStepsIfNeeded: END - savedDate=" + savedDate + ", currentDate=" + currentDate + ", previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsToday=" + stepsToday + ", stepsOffset=" + stepsOffset);
    }
    private void saveStepData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            editor.putString(KEY_SAVED_DATE, getCurrentDate());
        }
        editor.putInt(KEY_PREVIOUS_TOTAL_STEPS, previousTotalStepsFromSensor);
        editor.putInt(KEY_TOTAL_STEPS, totalStepsFromSensor); // Lưu totalSteps
        editor.putInt(KEY_STEPS_TODAY, stepsToday); // Lưu stepsToday
        editor.putInt(KEY_STEPS_OFFSET, stepsOffset);


        editor.apply();
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            Log.d(TAG, "Data saved: Date=" + getCurrentDate() + ", PreviousTotalSteps=" + previousTotalStepsFromSensor + ", TotalSteps=" + totalStepsFromSensor + ", StepsToday=" + stepsToday + ", Offset=" + stepsOffset);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int newTotalStepsFromSensor = (int) event.values[0];
            Log.d(TAG, "onSensorChanged: newTotalStepsFromSensor = " + newTotalStepsFromSensor);
            Log.d(TAG, "onSensorChanged: BEFORE - totalStepsFromSensor=" + totalStepsFromSensor + ", previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsOffset=" + stepsOffset + ", stepsToday=" + stepsToday);

            boolean firstRun = (totalStepsFromSensor == 0 && previousTotalStepsFromSensor == 0 && stepsOffset == 0); // Check for first run

            if (firstRun) {
                stepsOffset = newTotalStepsFromSensor; // Initial offset
                prefs.edit().putInt(KEY_STEPS_OFFSET, stepsOffset).apply();
                previousTotalStepsFromSensor = stepsOffset;
                Log.d(TAG, "onSensorChanged: FIRST RUN - Setting offset: " + stepsOffset);
            }

            if (newTotalStepsFromSensor < (previousTotalStepsFromSensor - stepsOffset)) { // Sensor reset
                Log.d(TAG, "onSensorChanged: SENSOR RESET - Re-calculating offset.");
                stepsOffset = previousTotalStepsFromSensor - newTotalStepsFromSensor; // Calculate new offset
                prefs.edit().putInt(KEY_STEPS_OFFSET, stepsOffset).apply();
            }

            totalStepsFromSensor = newTotalStepsFromSensor - stepsOffset;
            stepsToday = totalStepsFromSensor;

            if (stepsToday < 0) {
                Log.w(TAG, "onSensorChanged: NEGATIVE STEPS - Resetting.");
                stepsToday = 0;
            }
            Log.d(TAG, "onSensorChanged: AFTER - totalStepsFromSensor=" + totalStepsFromSensor + ", previousTotalStepsFromSensor=" + previousTotalStepsFromSensor + ", stepsOffset=" + stepsOffset + ", stepsToday=" + stepsToday);

            updateUI();
            saveStepData();
            Log.d(TAG, "onSensorChanged: Steps today: " + stepsToday);
        }
    }
    private void updateUI() {
        Log.d(TAG, "updateUI: stepsToday = " + stepsToday);
        tvStepCounter.setText(String.valueOf(stepsToday));
        tvTotalStepsSensor.setText("Tổng bước từ cảm biến: " + totalStepsFromSensor);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
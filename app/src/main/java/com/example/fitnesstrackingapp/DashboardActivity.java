package com.example.fitnesstrackingapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.example.fitnesstrackingapp.MainActivity.KEY_STEP_HISTORY;
import static com.example.fitnesstrackingapp.MainActivity.KEY_PREVIOUS_TOTAL_STEPS;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvStepsToday;
    private TextView tvTotalSteps;
    private TextView tvDashboardDate;
    private TextView tvAverageSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Tạo file layout này

        // Ánh xạ các TextView trong layout
        tvStepsToday = findViewById(R.id.tv_dashboard_steps_today);
        tvTotalSteps = findViewById(R.id.tv_dashboard_total_steps);
        tvDashboardDate = findViewById(R.id.tv_dashboard_date);
        tvAverageSteps = findViewById(R.id.tv_dashboard_average_steps);


        loadAndDisplayData();
    }

    private void loadAndDisplayData() {
        SharedPreferences prefs = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        int stepsToday = prefs.getInt("stepsToday", 0);
        int previousTotalSteps = prefs.getInt(KEY_PREVIOUS_TOTAL_STEPS, 0);
        String currentDate = prefs.getString("savedDate", "");

        Log.d("DashboardActivity", "loadAndDisplayData: stepsToday from prefs = " + stepsToday);

        int totalSteps = previousTotalSteps;
        int averageSteps = calculateAverageSteps(7);
        tvAverageSteps.setText("Trung bình (7 ngày): " + averageSteps + " bước");
        tvStepsToday.setText("Hôm nay: " + stepsToday + " bước");
        tvTotalSteps.setText("Tổng cộng: " + totalSteps + " bước");
        tvDashboardDate.setText("Ngày: " + currentDate);
    }
    //  Các hàm thống kê nâng cao (ví dụ: tính trung bình, vẽ biểu đồ) sẽ được thêm vào sau
    private int calculateAverageSteps(int numberOfDays) {
        SharedPreferences prefs = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        Set<String> allKeys = prefs.getAll().keySet();
        Set<String> stepHistoryKeys = new HashSet<>();
        for (String key : allKeys) {
            if (key.startsWith(KEY_STEP_HISTORY)) {
                stepHistoryKeys.add(key);
            }
        }

        List<Integer> lastDaysSteps = new ArrayList<>();
        List<String> sortedKeys = new ArrayList<>(stepHistoryKeys);
        sortedKeys.sort(String::compareTo); // Sort keys chronologically

        int keysToUse = Math.min(numberOfDays, sortedKeys.size());
        for (int i = sortedKeys.size() - keysToUse; i < sortedKeys.size(); i++) {
            lastDaysSteps.add(prefs.getInt(sortedKeys.get(i), 0));
        }

        if (lastDaysSteps.isEmpty()) {
            return 0; // Tránh chia cho 0
        }

        int sum = 0;
        for (int steps : lastDaysSteps) {
            sum += steps;
        }
        return sum / lastDaysSteps.size();
    }
}
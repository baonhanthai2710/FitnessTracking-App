package com.example.fitnesstrackingapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {
    
    private static final String TAG = "DashboardFragment";
    
    private TextView tvStepCounter;
    private TextView tvTotalStepsSensor;
    // private TextView tvDebugInfo; // Comment out since layout doesn't have this

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        tvStepCounter = view.findViewById(R.id.tv_step_counter);
        tvTotalStepsSensor = view.findViewById(R.id.tv_total_steps_sensor);
        
        // Comment out debug TextView until we add it to layout
        // tvDebugInfo = view.findViewById(R.id.tv_debug_info);
        
        Log.d(TAG, "DashboardFragment created");
        
        // Load and display saved step data immediately
        loadSavedStepData();
        
        return view;
    }
    
    private void loadSavedStepData() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
            int savedSteps = prefs.getInt(Constants.STEPS_TODAY_KEY, 0);
            int savedOffset = prefs.getInt(Constants.STEPS_OFFSET_KEY, 0);
            
            // Update UI with saved data
            updateStepCounter(savedSteps);
            
            // Comment out debug info until TextView is added
            // if (tvDebugInfo != null) {
            //     tvDebugInfo.setText("Saved steps: " + savedSteps + ", Offset: " + savedOffset);
            // }
            
            Log.d(TAG, "Loaded saved step data: " + savedSteps);
        }
    }
    
    public void updateStepCounter(int steps) {
        if (tvStepCounter != null) {
            tvStepCounter.setText(steps + " bước");
            Log.d(TAG, "Step counter updated: " + steps);
        } else {
            Log.w(TAG, "tvStepCounter is null, cannot update");
        }
    }
    
    public void updateTotalSteps(int steps) {
        if (tvTotalStepsSensor != null) {
            tvTotalStepsSensor.setText("Tổng: " + steps + " bước");
            Log.d(TAG, "Total steps updated: " + steps);
        } else {
            Log.w(TAG, "tvTotalStepsSensor is null, cannot update");
        }
        
        // Comment out debug info until TextView is added
        // if (tvDebugInfo != null && getContext() != null) {
        //     SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        //     int offset = prefs.getInt(Constants.STEPS_OFFSET_KEY, 0);
        //     String debugText = "Debug: Total=" + steps + ", Offset=" + offset + ", Today=" + (steps - offset);
        //     tvDebugInfo.setText(debugText);
        // }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "DashboardFragment resumed");
        // Reload step data when fragment becomes visible
        loadSavedStepData();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "DashboardFragment paused");
    }
}
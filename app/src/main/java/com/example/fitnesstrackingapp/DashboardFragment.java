package com.example.fitnesstrackingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {
    private TextView tvStepCounter;
    private TextView tvTotalStepsSensor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        tvStepCounter = view.findViewById(R.id.tv_step_counter);
        tvTotalStepsSensor = view.findViewById(R.id.tv_total_steps_sensor);
        
        // The activity will update the step counter values via these methods
        return view;
    }
    
    public void updateStepCounter(int steps) {
        if (tvStepCounter != null) {
            tvStepCounter.setText(steps + " bước");
        }
    }
    
    public void updateTotalSteps(int steps) {
        if (tvTotalStepsSensor != null) {
            tvTotalStepsSensor.setText(steps + " bước");
        }
    }
}
package com.example.fitnesstrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QuickAccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick_access, container, false);
        
        // Initialize buttons
        Button btnViewDashboard = view.findViewById(R.id.btn_view_dashboard);
        Button galleryButton = view.findViewById(R.id.gallery_button);
        Button cloudButton = view.findViewById(R.id.cloud_button);
        
        // Set click listeners
        btnViewDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DashboardActivity.class);
            startActivity(intent);
        });
        
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MediaGalleryActivity.class);
            startActivity(intent);
        });
        
        cloudButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CloudStorageActivity.class);
            startActivity(intent);
        });
        
        return view;
    }
}
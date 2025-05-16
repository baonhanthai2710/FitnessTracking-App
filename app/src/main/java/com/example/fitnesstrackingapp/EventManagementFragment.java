package com.example.fitnesstrackingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EventManagementFragment extends Fragment {
    private TextInputEditText etTitle;
    private TextInputEditText etDesc;
    private TextInputEditText etLocation;
    private TextView tvSelectedTime;
    private Spinner spType;
    private Calendar calendar;
    private InternalStorageHelper localHelper;
    private FirebaseHelper fbHelper;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);
        
        // Initialize form fields
        etTitle = view.findViewById(R.id.etTitle);
        etDesc = view.findViewById(R.id.etDesc);
        etLocation = view.findViewById(R.id.etLocation);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        spType = view.findViewById(R.id.spType);
        Button btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        
        // Initialize helpers
        localHelper = new InternalStorageHelper(requireContext());
        fbHelper = new FirebaseHelper(requireContext());
        
        // Initialize calendar
        calendar = Calendar.getInstance();
        
        // Set click listeners
        btnPickDateTime.setOnClickListener(v -> showDateTimePicker());
        
        MaterialButton btnSaveLocal = view.findViewById(R.id.btnSaveLocal);
        btnSaveLocal.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String type = spType.getSelectedItem().toString();
            
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ tiêu đề & mô tả", Toast.LENGTH_SHORT).show();
                return;
            }
            
            List<Event> events = localHelper.readEvents();
            String id = UUID.randomUUID().toString();
            long ts = System.currentTimeMillis();
            events.add(new Event(id, title, desc, location, type, ts));
            localHelper.saveEvents(events);
            Toast.makeText(requireContext(), "Đã lưu cục bộ", Toast.LENGTH_SHORT).show();
        });
        
        MaterialButton btnSync = view.findViewById(R.id.btnSync);
        btnSync.setOnClickListener(v -> {
            // Get the latest data from local storage
            List<Event> events = localHelper.readEvents();
            
            if (events.isEmpty()) {
                Toast.makeText(requireContext(), "Không có dữ liệu để đồng bộ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show progress bar (managed by activity)
            ProgressBar progressBar = requireActivity().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            
            fbHelper.syncEvents(events);
        });
        
        MaterialButton btnViewLocal = view.findViewById(R.id.btnViewLocal);
        btnViewLocal.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ViewLocalEventsActivity.class);
            startActivity(intent);
        });
        
        MaterialButton btnViewFirebase = view.findViewById(R.id.btnViewFirebase);
        btnViewFirebase.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ViewFirebaseEventsActivity.class);
            startActivity(intent);
        });
        
        return view;
    }
    
    private void showDateTimePicker() {
        calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            calendar.set(year, month, day);
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        
        datePicker.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            updateSelectedTimeDisplay();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        
        timePicker.show();
    }
    
    private void updateSelectedTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvSelectedTime.setText(sdf.format(calendar.getTime()));
    }
}
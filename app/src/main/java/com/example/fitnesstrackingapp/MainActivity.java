package com.example.fitnesstrackingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private TextView tvSelectedTime;
    private Calendar calendar;
    private Spinner spType;
    private EditText etTitle, etDesc, etLocation;
    private InternalStorageHelper localHelper;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display from version 1
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_main);
        
        // Apply window insets from version 1
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize form elements from version 2
        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        etLocation = findViewById(R.id.etLocation);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        calendar = Calendar.getInstance();
        spType = findViewById(R.id.spType);
        Button btnPickDateTime = findViewById(R.id.btnPickDateTime);
        
        // Initialize helpers from version 2
        localHelper = new InternalStorageHelper(this);
        fbHelper = new FirebaseHelper(this);

        // Media management buttons from version 1
        Button galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MediaGalleryActivity.class);
            startActivity(intent);
        });
        
        Button cloudButton = findViewById(R.id.cloud_button);
        cloudButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CloudStorageActivity.class);
            startActivity(intent);
        });

        // Date/time picker from version 2
        btnPickDateTime.setOnClickListener(v -> showDateTimePicker());

        // Event save and sync functionality from version 2
        findViewById(R.id.btnSaveLocal).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String type = spType.getSelectedItem().toString();
            
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ tiêu đề & mô tả", Toast.LENGTH_SHORT).show();
                return;
            }
            
            List<Event> Events = localHelper.readEvents();
            String id = UUID.randomUUID().toString();
            long ts = System.currentTimeMillis();
            Events.add(new Event(id, title, desc, location, type, ts));
            localHelper.saveEvents(Events);
            Toast.makeText(this, "Đã lưu cục bộ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSync).setOnClickListener(v -> {
            // Đảm bảo đọc lại dữ liệu mới nhất từ local
            List<Event> events = localHelper.readEvents();

            if (events.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để đồng bộ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị progress bar
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            fbHelper.syncEvents(events);
        });
        
        // View event buttons from version 2
        findViewById(R.id.btnViewLocal).setOnClickListener(v ->
                startActivity(new Intent(this, ViewLocalEventsActivity.class))
        );
        
        findViewById(R.id.btnViewFirebase).setOnClickListener(v ->
                startActivity(new Intent(this, ViewFirebaseEventsActivity.class))
        );
    }

    // Date/time picker methods from version 2
    private void showDateTimePicker() {
        calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
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
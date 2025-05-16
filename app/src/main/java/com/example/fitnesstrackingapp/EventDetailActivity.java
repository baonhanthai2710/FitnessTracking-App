package com.example.fitnesstrackingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Event event = (Event) getIntent().getSerializableExtra("event");

        TextView tvTitle = findViewById(R.id.tvTitle);
        Chip chipEventType = findViewById(R.id.chipEventType); // Thay đổi ở đây
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvDescription = findViewById(R.id.tvDescription);

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'ngày' dd/MM/yyyy", Locale.getDefault());
        String formattedTime = sdf.format(new Date(event.getTimestamp()));

        // Hiển thị dữ liệu
        tvTitle.setText(event.getTitle());
        chipEventType.setText(event.getType().equals("appointment") ? "HẸN BÁC SĨ" : "SỰ KIỆN"); // Sửa ở đây
        tvDateTime.setText(formattedTime);
        tvLocation.setText(event.getLocation()); // Đã remove prefix "Địa điểm:"
        tvDescription.setText(event.getDescription());
    }
}
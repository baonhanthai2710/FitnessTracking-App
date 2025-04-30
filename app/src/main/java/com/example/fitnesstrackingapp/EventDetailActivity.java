package com.example.fitnesstrackingapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        TextView tvType = findViewById(R.id.tvType);
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvDescription = findViewById(R.id.tvDescription);

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'ngày' dd/MM/yyyy", Locale.getDefault());
        String formattedTime = sdf.format(new Date(event.getTimestamp()));

        // Hiển thị dữ liệu
        tvTitle.setText(event.getTitle());
        tvType.setText("Loại: " + (event.getType().equals("appointment") ? "Hẹn bác sĩ" : "Sự kiện"));
        tvDateTime.setText(formattedTime);
        tvLocation.setText("Địa điểm: " + event.getLocation());
        tvDescription.setText(event.getDescription());
    }
}
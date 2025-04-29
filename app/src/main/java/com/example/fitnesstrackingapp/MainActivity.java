package com.example.fitnesstrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private EditText etTitle, etDesc;
    private InternalStorageHelper localHelper;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        etTitle = findViewById(R.id.etTitle);
        etDesc  = findViewById(R.id.etDesc);
        localHelper = new InternalStorageHelper(this);
        fbHelper    = new FirebaseHelper(this);

        findViewById(R.id.btnSaveLocal).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc  = etDesc.getText().toString().trim();
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this,"Vui lòng nhập đủ tiêu đề & mô tả",Toast.LENGTH_SHORT).show();
                return;
            }
            List<Event> Events = localHelper.readEvents();
            String id = UUID.randomUUID().toString();
            long ts   = System.currentTimeMillis();
            Events.add(new Event(id, title, desc, ts));
            localHelper.saveEvents(Events);
            Toast.makeText(this,"Đã lưu cục bộ",Toast.LENGTH_SHORT).show();
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

        findViewById(R.id.btnViewLocal).setOnClickListener(v ->
                startActivity(new Intent(this, ViewLocalEventsActivity.class))
        );
        findViewById(R.id.btnViewFirebase).setOnClickListener(v ->
                startActivity(new Intent(this, ViewFirebaseEventsActivity.class))
        );
    }
}

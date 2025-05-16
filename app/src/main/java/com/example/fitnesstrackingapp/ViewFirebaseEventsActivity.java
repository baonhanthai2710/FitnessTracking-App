package com.example.fitnesstrackingapp;


import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.*;

public class ViewFirebaseEventsActivity extends AppCompatActivity {
    private FirebaseHelper fbHelper;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_events);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        toolbar.setNavigationOnClickListener(v -> finish());                  // trả về Activity trước đó :contentReference[oaicite:6]{index=6}

            // Sử dụng Application Context
        fbHelper = new FirebaseHelper(getApplicationContext());
        fbHelper.fetchEvents(new FirebaseHelper.DataStatus() {
            @Override
            public void onSuccess(List<Event> events) {

                // 2. Sắp xếp mới → cũ
                EventUtils.sortByTimestamp(events, false);  // đảm bảo thứ tự đúng :contentReference[oaicite:11]{index=11}

                adapter = new EventAdapter(events, event -> {
                    fbHelper.deleteEvent(event.getId());
                    adapter.notifyDataSetChanged();
                });
                rv.setAdapter(adapter);
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(ViewFirebaseEventsActivity.this,
                        "Lỗi: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fbHelper.detachListener();  // gỡ listener :contentReference[oaicite:13]{index=13}
    }
}


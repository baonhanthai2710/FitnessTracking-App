package com.example.fitnesstrackingapp;


import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.*;

public class ViewLocalEventsActivity extends AppCompatActivity {
    private InternalStorageHelper localHelper;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_events);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());                  // trả về Activity trước đó :contentReference[oaicite:6]{index=6}

        RecyclerView rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);

        localHelper = new InternalStorageHelper(this);
        List<Event> events = localHelper.readEvents();

        // 1. Sắp xếp mới → cũ (ascending = false)
        EventUtils.sortByTimestamp(events, false);  // mới nhất lên đầu :contentReference[oaicite:8]{index=8}

        adapter = new EventAdapter(events, event -> {
            localHelper.deleteEvent(event.getId());
//            adapter.notifyDataSetChanged();
            adapter.removeEvent(event);
        });
        rv.setAdapter(adapter);

        // SearchView filter
        SearchView sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String q) {
                adapter.getFilter().filter(q);
                return false;
            }
        });
    }
}


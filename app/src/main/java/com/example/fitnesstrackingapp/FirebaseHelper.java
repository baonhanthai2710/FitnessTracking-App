package com.example.fitnesstrackingapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.util.*;

public class FirebaseHelper {
    private static FirebaseDatabase database; // Dùng static instance
    private DatabaseReference dbRef;
    private Context context;

    public FirebaseHelper(Context context) {
        this.context = context;

        // Đảm bảo chỉ khởi tạo 1 lần
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        dbRef = database.getReference("events");
    }

    // Sync tasks lên Firebase với listener
    public void syncEvents(List<Event> events) {
        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            Toast.makeText(context, "Không có kết nối Internet", Toast.LENGTH_LONG).show();
            return;
        }

        for (Event t : events) {
            // Thêm debug log
            Log.d("FIREBASE_SYNC", "Attempting to sync event: " + t.getId());

            dbRef.child(t.getId())
                    .setValue(t)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FIREBASE_SYNC", "Sync success: " + t.getId());
                        Toast.makeText(context, "Đã sync: " + t.getTitle(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_SYNC", "Sync error: " + e.getMessage(), e);
                        Toast.makeText(context, "Lỗi sync: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    // Hàm kiểm tra kết nối mạng
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Xóa task theo ID
    public void deleteEvent(String id) {
        dbRef.child(id)
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Đã xóa trên Firebase", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );  // dùng removeValue() để xóa node :contentReference[oaicite:9]{index=9}
    }

    // Fetch tasks từ Firebase
    private ValueEventListener listener;
    public void fetchEvents(final DataStatus dataStatus) {
        listener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event t = child.getValue(Event.class);
                    list.add(t);
                }
                dataStatus.onSuccess(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dataStatus.onError(error.toException());
            }
        });
    }

    // Gỡ listener để tránh callback dư thừa :contentReference[oaicite:10]{index=10}
    public void detachListener() {
        if (listener != null) dbRef.removeEventListener(listener);
    }

    public interface DataStatus {
        void onSuccess(List<Event> events);
        void onError(Exception e);
    }
}


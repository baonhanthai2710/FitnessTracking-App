package com.example.fitnesstrackingapp;


import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;

import java.util.*;

public class FirebaseHelper {
    private DatabaseReference dbRef;
    private Context context;

    public FirebaseHelper(Context context) {
        this.context = context;
        dbRef = FirebaseDatabase.getInstance().getReference("events");
    }

    // Sync tasks lên Firebase với listener
    public void syncEvents(List<Event> events) {
        for (Event t : events) {
            dbRef.child(t.getId())
                    .setValue(t)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Đã sync: " + t.getTitle(), Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi sync: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
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


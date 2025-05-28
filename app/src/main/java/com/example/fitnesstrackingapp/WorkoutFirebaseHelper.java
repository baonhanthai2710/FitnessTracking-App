package com.example.fitnesstrackingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFirebaseHelper {
    private static FirebaseDatabase database;
    private DatabaseReference dbRef;
    private Context context;
    private ValueEventListener listener;

    public WorkoutFirebaseHelper(Context context) {
        this.context = context;
        
        // Đảm bảo chỉ khởi tạo 1 lần
        try {
        // Thay đổi ở đây - sử dụng URL cụ thể cho khu vực châu Á
        if (database == null) {
            database = FirebaseDatabase.getInstance("https://fitnessapp-bbf6f-default-rtdb.asia-southeast1.firebasedatabase.app");
            Log.d("FIREBASE_INIT", "Firebase initialization with Asia region successful");
        }
            dbRef = database.getReference("workouts");
            Log.d("FIREBASE_INIT", "Reference path: " + dbRef.toString());
        } catch (Exception e) {
            Log.e("FIREBASE_INIT", "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
    
    // Sync workouts lên Firebase
    public void syncWorkouts(List<Workout> workouts) {
        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            Toast.makeText(context, "Không có kết nối Internet", Toast.LENGTH_LONG).show();
            return;
        }

        for (Workout w : workouts) {
            // Tạo ID nếu chưa có
            if (w.getId() == null || w.getId().isEmpty()) {
                w.setId(dbRef.push().getKey());
            }
            
            // Thêm debug log
            Log.d("FIREBASE_SYNC", "Attempting to sync workout: " + w.getId());

            dbRef.child(w.getId())
                .setValue(w)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_SYNC", "Sync success: " + w.getId());
                    Toast.makeText(context, "Đã sync: " + w.getType(), Toast.LENGTH_SHORT).show();
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
    
    // Xóa workout theo ID
    public void deleteWorkout(String id) {
        dbRef.child(id)
            .removeValue()
            .addOnSuccessListener(aVoid ->
                Toast.makeText(context, "Đã xóa trên Firebase", Toast.LENGTH_SHORT).show()
            )
            .addOnFailureListener(e ->
                Toast.makeText(context, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
    
    // Lấy workouts từ Firebase
    public void fetchWorkouts(final DataStatus dataStatus) {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Workout> workouts = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Workout workout = ds.getValue(Workout.class);
                    workouts.add(workout);
                }
                dataStatus.DataIsLoaded(workouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FIREBASE_FETCH", "Error fetching data", databaseError.toException());
                Toast.makeText(context, "Lỗi tải dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        dbRef.addValueEventListener(listener);
    }
    
    // Gỡ listener để tránh callback dư thừa
    public void detachListener() {
        if (listener != null) {
            dbRef.removeEventListener(listener);
        }
    }
    
    public interface DataStatus {
        void DataIsLoaded(List<Workout> workouts);
    }
}

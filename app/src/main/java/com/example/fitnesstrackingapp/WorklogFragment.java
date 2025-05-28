package com.example.fitnesstrackingapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class WorklogFragment extends Fragment implements WorkoutAdapter.WorkoutActionListener {
    
    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList = new ArrayList<>();
    private FloatingActionButton fabAddWorkout;
    private View addWorkoutForm;
    
    // UI for add/edit form
    private Spinner spinnerWorkoutType;
    private EditText etDuration;
    private EditText etCalories;
    private EditText etNotes;
    private Button btnSaveWorkout;
    private Button btnCancel;
    
    private boolean isEditMode = false;
    private int editPosition = -1;
    
    // Firebase helper
    private WorkoutFirebaseHelper firebaseHelper;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worklog, container, false);
        
        // Khởi tạo Firebase helper
        firebaseHelper = new WorkoutFirebaseHelper(getContext());
        
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.rv_workouts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter with empty list and this as the listener
        workoutAdapter = new WorkoutAdapter(workoutList, this);
        recyclerView.setAdapter(workoutAdapter);
        
        // Get references to views
        fabAddWorkout = view.findViewById(R.id.fab_add_workout);
        addWorkoutForm = view.findViewById(R.id.layout_add_workout_form);
        
        // Set click listener for FAB
        fabAddWorkout.setOnClickListener(v -> {
            isEditMode = false;
            editPosition = -1;
            showAddWorkoutForm();
        });
        
        // Set up the form views
        spinnerWorkoutType = view.findViewById(R.id.spinner_workout_type);
        etDuration = view.findViewById(R.id.et_duration);
        etCalories = view.findViewById(R.id.et_calories);
        etNotes = view.findViewById(R.id.et_notes);
        btnSaveWorkout = view.findViewById(R.id.btn_save_workout);
        btnCancel = view.findViewById(R.id.btn_cancel);
        
        // Hide form initially
        addWorkoutForm.setVisibility(View.GONE);
        
        // Set up save workout button
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());
        
        // Set up cancel button
        btnCancel.setOnClickListener(v -> hideWorkoutForm());
        
        // Tải dữ liệu từ Firebase thay vì loadSampleWorkouts()
        loadWorkoutsFromFirebase();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload khi quay lại tab
        loadWorkoutsFromFirebase();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Detach firebase listener khi rời tab
        firebaseHelper.detachListener();
    }
    
    private void loadWorkoutsFromFirebase() {
        firebaseHelper.fetchWorkouts(new WorkoutFirebaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Workout> workouts) {
                workoutList.clear();
                workoutList.addAll(workouts);
                
                // Sắp xếp theo thời gian mới nhất trước
                Collections.sort(workoutList, (w1, w2) -> 
                    Long.compare(w2.getTimestamp(), w1.getTimestamp()));
                
                workoutAdapter.notifyDataSetChanged();
                
                if (workoutList.isEmpty()) {
                    // Nếu không có dữ liệu, tạo dữ liệu mẫu
                    loadSampleWorkouts();
                }
            }
        });
    }
    
    private void saveWorkout() {
        // Validate form
        if (etDuration.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter duration", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get values from form
        String type = spinnerWorkoutType.getSelectedItem().toString();
        int duration = Integer.parseInt(etDuration.getText().toString());
        int calories = etCalories.getText().toString().isEmpty() ? 0 : 
                Integer.parseInt(etCalories.getText().toString());
        String notes = etNotes.getText().toString();
        
        if (isEditMode && editPosition >= 0 && editPosition < workoutList.size()) {
            // Update existing workout
            Workout workout = workoutList.get(editPosition);
            workout.setType(type);
            workout.setDuration(duration);
            workout.setCaloriesBurned(calories);
            workout.setNotes(notes);
            // Date remains unchanged
            
            workoutAdapter.notifyItemChanged(editPosition);
            Toast.makeText(getContext(), "Workout updated", Toast.LENGTH_SHORT).show();
            
            // Sync to Firebase
            List<Workout> updatedWorkout = new ArrayList<>();
            updatedWorkout.add(workout);
            firebaseHelper.syncWorkouts(updatedWorkout);
        } else {
            // Create new workout
            Workout workout = new Workout(type, duration, calories, notes, new Date());
            
            // Add to beginning of list
            workoutList.add(0, workout);
            workoutAdapter.notifyItemInserted(0);
            
            // Sync to Firebase
            List<Workout> newWorkout = new ArrayList<>();
            newWorkout.add(workout);
            firebaseHelper.syncWorkouts(newWorkout);
            
            // Scroll to top
            recyclerView.smoothScrollToPosition(0);
            
            Toast.makeText(getContext(), "Workout saved", Toast.LENGTH_SHORT).show();
        }
        
        // Reset form and hide
        resetAndHideForm();
    }
    
    private void resetAndHideForm() {
        etDuration.setText("");
        etCalories.setText("");
        etNotes.setText("");
        isEditMode = false;
        editPosition = -1;
        hideWorkoutForm();
    }
    
    private void showAddWorkoutForm() {
        // Update button text based on mode
        btnSaveWorkout.setText(isEditMode ? "Update" : "Save");
        
        addWorkoutForm.setVisibility(View.VISIBLE);
        fabAddWorkout.hide();
    }
    
    private void hideWorkoutForm() {
        addWorkoutForm.setVisibility(View.GONE);
        fabAddWorkout.show();
    }
    
    private void loadSampleWorkouts() {
        // Add some sample workouts
        Workout workout1 = new Workout("Running", 45, 350, "Morning run in the park", new Date());
        Workout workout2 = new Workout("Weight Training", 60, 420, "Chest and back day", new Date(System.currentTimeMillis() - 86400000)); // Yesterday
        Workout workout3 = new Workout("Swimming", 30, 250, "20 laps", new Date(System.currentTimeMillis() - 2 * 86400000)); // 2 days ago
        
        // Thêm vào list và sync lên Firebase
        workoutList.add(workout1);
        workoutList.add(workout2);
        workoutList.add(workout3);
        
        workoutAdapter.notifyDataSetChanged();
        
        // Sync to Firebase
        firebaseHelper.syncWorkouts(workoutList);
    }

    @Override
    public void onEditWorkout(Workout workout, int position) {
        // Set edit mode
        isEditMode = true;
        editPosition = position;
        
        // Fill form with workout data
        int typePosition = getTypePosition(workout.getType());
        spinnerWorkoutType.setSelection(typePosition);
        etDuration.setText(String.valueOf(workout.getDuration()));
        etCalories.setText(String.valueOf(workout.getCaloriesBurned()));
        etNotes.setText(workout.getNotes());
        
        // Show form
        showAddWorkoutForm();
    }

    @Override
    public void onDeleteWorkout(Workout workout, final int position) {
        // Show confirmation dialog
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Lưu tạm workout để khôi phục nếu cần
                final Workout deletedWorkout = workoutList.get(position);
                final int deletedPosition = position;
                
                // Remove from list
                workoutList.remove(position);
                workoutAdapter.notifyItemRemoved(position);
                
                // Xóa trên Firebase
                if (deletedWorkout.getId() != null) {
                    firebaseHelper.deleteWorkout(deletedWorkout.getId());
                }
                
                // Hiển thị Snackbar với tùy chọn hoàn tác
                Snackbar.make(getView(), "Workout deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> {
                        // Khôi phục workout nếu người dùng chọn UNDO
                        workoutList.add(deletedPosition, deletedWorkout);
                        workoutAdapter.notifyItemInserted(deletedPosition);
                        
                        // Sync lại lên Firebase
                        List<Workout> restoredWorkout = new ArrayList<>();
                        restoredWorkout.add(deletedWorkout);
                        firebaseHelper.syncWorkouts(restoredWorkout);
                    })
                    .show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private int getTypePosition(String type) {
        String[] types = getResources().getStringArray(R.array.workout_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type)) {
                return i;
            }
        }
        return 0; // Default to first position
    }
}
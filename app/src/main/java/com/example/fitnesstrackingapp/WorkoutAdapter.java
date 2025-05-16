package com.example.fitnesstrackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    
    private List<Workout> workoutList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
    private WorkoutActionListener listener;
    
    public interface WorkoutActionListener {
        void onEditWorkout(Workout workout, int position);
        void onDeleteWorkout(Workout workout, int position);
    }
    
    public WorkoutAdapter(List<Workout> workoutList, WorkoutActionListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        
        holder.tvWorkoutType.setText(workout.getType());
        holder.tvDuration.setText(workout.getDuration() + " min");
        holder.tvCalories.setText(workout.getCaloriesBurned() + " cal");
        holder.tvDate.setText(dateFormat.format(workout.getDate()));
        
        if (!workout.getNotes().isEmpty()) {
            holder.tvNotes.setText(workout.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
        
        // Set workout type icon
        switch (workout.getType().toLowerCase()) {
            case "running":
                holder.ivWorkoutIcon.setImageResource(R.drawable.ic_run);
                break;
            case "weight training":
                holder.ivWorkoutIcon.setImageResource(R.drawable.ic_weight);
                break;
            case "swimming":
                holder.ivWorkoutIcon.setImageResource(R.drawable.ic_swim);
                break;
            case "cycling":
                holder.ivWorkoutIcon.setImageResource(R.drawable.ic_bike);
                break;
            default:
                holder.ivWorkoutIcon.setImageResource(R.drawable.ic_fitness);
                break;
        }
        
        // Set up action buttons
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditWorkout(workout, holder.getAdapterPosition());
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteWorkout(workout, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return workoutList.size();
    }
    
    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkoutIcon;
        TextView tvWorkoutType;
        TextView tvDuration;
        TextView tvCalories;
        TextView tvDate;
        TextView tvNotes;
        ImageButton btnEdit;
        ImageButton btnDelete;
        
        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkoutIcon = itemView.findViewById(R.id.iv_workout_icon);
            tvWorkoutType = itemView.findViewById(R.id.tv_workout_type);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            btnEdit = itemView.findViewById(R.id.btn_edit_workout);
            btnDelete = itemView.findViewById(R.id.btn_delete_workout);
        }
    }
}
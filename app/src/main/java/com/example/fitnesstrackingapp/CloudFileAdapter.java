package com.example.fitnesstrackingapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CloudFileAdapter extends RecyclerView.Adapter<CloudFileAdapter.FileViewHolder> {

    private Context context;
    private List<CloudFile> fileList;
    private SimpleDateFormat dateFormat;

    public CloudFileAdapter(Context context, List<CloudFile> fileList) {
        this.context = context;
        this.fileList = fileList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cloud_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        CloudFile file = fileList.get(position);
        
        holder.fileName.setText(file.getName());
        holder.fileType.setText(file.getFileType());
        
        if (file.getTimestamp() > 0) {
            holder.fileDate.setText(dateFormat.format(new Date(file.getTimestamp())));
            holder.fileDate.setVisibility(View.VISIBLE);
        } else {
            holder.fileDate.setVisibility(View.GONE);
        }
        
        // Set file icon based on type
        if (file.getFileType().equals("Image")) {
            // Load thumbnail for images
            Glide.with(context)
                 .load(file.getDownloadUrl())
                 .centerCrop()
                 .into(holder.fileIcon);
        } else if (file.getFileType().equals("Document")) {
            holder.fileIcon.setImageResource(R.drawable.ic_document);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file);
        }
        
        // Set click listeners
        holder.btnShare.setOnClickListener(v -> shareFile(file));
        holder.btnDelete.setOnClickListener(v -> deleteFile(file, position));
        
        // Open file on item click
        holder.itemView.setOnClickListener(v -> openFile(file));
    }
    
    private void openFile(CloudFile file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(file.getDownloadUrl()));
        context.startActivity(intent);
    }
    
    private void shareFile(CloudFile file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this file: " + file.getDownloadUrl());
        shareIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
    
    private void deleteFile(CloudFile file, int position) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReference(file.getStoragePath());
        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from list and update UI
                    fileList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete file: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName, fileType, fileDate;
        MaterialButton btnShare, btnDelete;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileName = itemView.findViewById(R.id.file_name);
            fileType = itemView.findViewById(R.id.file_type);
            fileDate = itemView.findViewById(R.id.file_date);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
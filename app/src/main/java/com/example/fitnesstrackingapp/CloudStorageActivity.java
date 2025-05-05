package com.example.fitnesstrackingapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class CloudStorageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private RecyclerView recyclerView;
    private CloudFileAdapter adapter;
    private List<CloudFile> fileList = new ArrayList<>();
    
    private ProgressBar progressBar;
    private TextView progressText;
    private FloatingActionButton fabUpload;
    
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedFile);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_storage);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Anonymous sign-in for simplicity
        signInAnonymously();
        
        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CloudFileAdapter(this, fileList);
        recyclerView.setAdapter(adapter);
        
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        fabUpload = findViewById(R.id.fab_upload);
        
        fabUpload.setOnClickListener(v -> showFileTypeSelection());
    }
    
    private void showFileTypeSelection() {
        String[] fileTypes = {"Image", "Medical Record", "Progress Report", "Other Document"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_file_type)
               .setItems(fileTypes, (dialog, which) -> {
                   String mimeType;
                   switch (which) {
                       case 0: // Image
                           mimeType = "image/*";
                           break;
                       case 1: // Medical Record
                       case 2: // Progress Report
                       case 3: // Other Document
                           mimeType = "*/*";
                           break;
                       default:
                           mimeType = "*/*";
                   }
                   getContent.launch(mimeType);
               });
        builder.create().show();
    }
    
    private void handleSelectedFile(Uri fileUri) {
        if (fileUri == null) return;
        
        String fileName = getFileNameFromUri(fileUri);
        String fileType = getContentResolver().getType(fileUri);
        
        // Start upload
        uploadFile(fileUri, fileName, fileType);
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    
    private void uploadFile(Uri fileUri, String fileName, String fileType) {
        if (mAuth.getCurrentUser() == null) {
            signInAnonymously();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        
        // Determine folder based on file type
        String folder = "other";
        if (fileType != null) {
            if (fileType.startsWith("image/")) {
                folder = "images";
            } else if (fileType.equals("application/pdf")) {
                folder = "documents";
            }
        }
        
        // Create storage reference
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child("users/" + userId + "/" + folder + "/" + fileName);
        
        // Upload file
        UploadTask uploadTask = fileRef.putFile(fileUri);
        
        // Monitor upload progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
            progressText.setText(String.format("%.0f%%", progress));
        }).addOnSuccessListener(taskSnapshot -> {
            // Get download URL
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Hide progress
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                
                // Create cloud file object
                CloudFile cloudFile = new CloudFile(
                        fileName,
                        downloadUri.toString(),
                        fileRef.getPath(),
                        fileType,
                        System.currentTimeMillis()
                );
                
                // Add to list and update UI
                fileList.add(0, cloudFile);
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                
                Toast.makeText(CloudStorageActivity.this, R.string.upload_success, Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Hide progress
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            
            Toast.makeText(CloudStorageActivity.this, getString(R.string.upload_error) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    
    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Signed in successfully, now load files
                        loadUserFiles();
                    } else {
                        Toast.makeText(CloudStorageActivity.this, 
                                "Authentication failed: " + task.getException(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void loadUserFiles() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference userRef = storageRef.child("users/" + userId);
        
        userRef.listAll()
                .addOnSuccessListener(listResult -> {
                    fileList.clear();
                    
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            CloudFile file = new CloudFile(
                                    item.getName(),
                                    uri.toString(),
                                    item.getPath(),
                                    null,
                                    0
                            );
                            fileList.add(file);
                            adapter.notifyDataSetChanged();
                        });
                    }
                    
                    // Also check subdirectories
                    for (StorageReference prefix : listResult.getPrefixes()) {
                        loadFilesFromPrefix(prefix);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CloudStorageActivity.this, 
                            "Failed to load files: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadFilesFromPrefix(StorageReference prefix) {
        prefix.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            CloudFile file = new CloudFile(
                                    item.getName(),
                                    uri.toString(),
                                    item.getPath(),
                                    null,
                                    0
                            );
                            fileList.add(file);
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
    }
}
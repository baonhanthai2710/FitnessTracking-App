package com.example.fitnesstrackingapp;

public class CloudFile {
    private String name;
    private String downloadUrl;
    private String storagePath;
    private String mimeType;
    private long timestamp;

    public CloudFile(String name, String downloadUrl, String storagePath, String mimeType, long timestamp) {
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public String getFileType() {
        if (mimeType == null) {
            // Try to guess based on name
            String lowerName = name.toLowerCase();
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                lowerName.endsWith(".png") || lowerName.endsWith(".gif")) {
                return "Image";
            } else if (lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || 
                       lowerName.endsWith(".docx")) {
                return "Document";
            } else {
                return "Other";
            }
        }
        
        if (mimeType.startsWith("image/")) {
            return "Image";
        } else if (mimeType.equals("application/pdf") || 
                  mimeType.equals("application/msword") || 
                  mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "Document";
        } else {
            return "Other";
        }
    }
}
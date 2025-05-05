package com.example.fitnesstrackingapp;

import android.net.Uri;

public class MediaItem {
    private Uri uri;
    private String name;

    public MediaItem(Uri uri, String name) {
        this.uri = uri;
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }
}
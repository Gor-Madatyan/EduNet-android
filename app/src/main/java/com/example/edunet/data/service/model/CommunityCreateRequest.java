package com.example.edunet.data.service.model;

import android.net.Uri;

public class CommunityCreateRequest {
    private String name;
    private String description;
    private Uri avatar;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Uri getAvatar() {
        return avatar;
    }
    public CommunityCreateRequest setName(String name) {
        this.name = name;
        return this;
    }

    public CommunityCreateRequest setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommunityCreateRequest setAvatar(Uri avatar) {
        this.avatar = avatar;
        return this;
    }
}

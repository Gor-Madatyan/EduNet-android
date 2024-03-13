package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.NonNull;

public class CommunityUpdateRequest {
    private final String id;
    private String name;
    private String description;
    private Uri avatar;

    private boolean isNameSet = false;
    private boolean isDescriptionSet = false;
    private boolean isAvatarSet = false;

    public CommunityUpdateRequest(@NonNull String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isNameSet() {
        return isNameSet;
    }

    public CommunityUpdateRequest setName(String name) {
        this.name = name;
        isNameSet = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isDescriptionSet() {
        return isDescriptionSet;
    }

    public CommunityUpdateRequest setDescription(String description) {
        this.description = description;
        isDescriptionSet = true;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvatarSet() {
        return isAvatarSet;
    }

    public CommunityUpdateRequest setAvatar(Uri avatar) {
        this.avatar = avatar;
        isAvatarSet = true;
        return this;
    }

    public Uri getAvatar() {
        return avatar;
    }


}

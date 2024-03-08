package com.example.edunet.data.service.model;

import android.net.Uri;

public class CommunityModifyRequest {
    private String name;
    private String description;
    private Uri avatar;

    private boolean isNameSet = false;
    private boolean isDescriptionSet = false;
    private boolean isAvatarSet = false;

    public boolean isNameSet() {
        return isNameSet;
    }

    public CommunityModifyRequest setName(String name) {
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

    public CommunityModifyRequest setDescription(String description) {
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

    public CommunityModifyRequest setAvatar(Uri avatar) {
        this.avatar = avatar;
        isAvatarSet = true;
        return this;
    }

    public Uri getAvatar() {
        return avatar;
    }


}

package com.example.edunet.data.service.model;

import android.net.Uri;


/**
 * User update request that receives optional name and external avatar url to update user profile
 */
public class UserUpdateRequest {
    private String name;
    private Uri avatar;

    private boolean isNameSet = false;
    private boolean isAvatarSet = false;

    public boolean isNameSet() {
        return isNameSet;
    }

    public boolean isAvatarSet() {
        return isAvatarSet;
    }

    public String getName() {
        return name;
    }

    public Uri getAvatar() {
        return avatar;
    }

    public UserUpdateRequest setName(String name) {
        this.name = name;
        isNameSet = true;
        return this;
    }

    public UserUpdateRequest setAvatar(Uri photoUri) {
        avatar = photoUri;
        isAvatarSet = true;
        return this;
    }
}

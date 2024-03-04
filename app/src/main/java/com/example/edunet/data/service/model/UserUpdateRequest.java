package com.example.edunet.data.service.model;

import android.net.Uri;


/**
 * User update request that receives optional name and external avatar url to update user profile
 */
public class UserUpdateRequest {
    private String name;
    private String bio;
    private Uri avatar;

    private boolean isNameSet = false;
    private boolean isBioSet = false;
    private boolean isAvatarSet = false;

    public boolean isNameSet() {
        return isNameSet;
    }

    public UserUpdateRequest setName(String name) {
        this.name = name;
        isNameSet = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isBioSet() {
        return isBioSet;
    }

    public UserUpdateRequest setBio(String bio) {
        this.bio = bio;
        isBioSet = true;
        return this;
    }

    public String getBio() {
        return bio;
    }

    public boolean isAvatarSet() {
        return isAvatarSet;
    }

    public UserUpdateRequest setAvatar(Uri photoUri) {
        avatar = photoUri;
        isAvatarSet = true;
        return this;
    }

    public Uri getAvatar() {
        return avatar;
    }


}

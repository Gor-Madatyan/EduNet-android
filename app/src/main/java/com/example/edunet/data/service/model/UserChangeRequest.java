package com.example.edunet.data.service.model;

import android.net.Uri;

public class UserChangeRequest {
    private String name;
    private Uri photo;

    private boolean isNameSet = false;
    private boolean isPhotoSet = false;

    public boolean isNameSet() {
        return isNameSet;
    }

    public boolean isPhotoSet() {
        return isPhotoSet;
    }

    public String getName() {
        return name;
    }

    public Uri getPhoto() {
        return photo;
    }

    public UserChangeRequest setName(String name) {
        this.name = name;
        isNameSet = true;
        return this;
    }

    public UserChangeRequest setPhoto(Uri photoUri) {
        photo = photoUri;
        isPhotoSet = true;
        return this;
    }
}

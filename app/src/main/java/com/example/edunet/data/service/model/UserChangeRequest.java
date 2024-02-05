package com.example.edunet.data.service.model;

public class UserChangeRequest {
    private String newName;
    private String newPhotoUri;

    private boolean isNewNameSet = false;
    private boolean isNewPhotoUriSet = false;

    public boolean isNewNameSet() {
        return isNewNameSet;
    }

    public boolean isNewPhotoUriSet() {
        return isNewPhotoUriSet;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewPhotoUri() {
        return newPhotoUri;
    }

    public UserChangeRequest setNewName(String name) {
        newName = name;
        isNewNameSet = true;
        return this;
    }

    public UserChangeRequest setNewPhotoUri(String photoUri) {
        newPhotoUri = photoUri;
        isNewPhotoUriSet = true;
        return this;
    }
}

package com.example.edunet.data.service.model;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;


/**
 * User update request that receives optional name and local avatar uri to update user profile
 */
public class UserUpdateRequest {
    private String name;
    private String bio;
    private Uri avatar;

    private boolean isNameSet = false;
    private boolean isBioSet = false;
    private boolean isAvatarSet = false;

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();

        if(isNameSet()) map.put("name", getName());
        if(isAvatarSet()) map.put("avatar", getAvatar() == null ? null: getAvatar().toString());
        if(isBioSet()) map.put("bio", getBio());

        return map;
    }

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

    public UserUpdateRequest setAvatar(Uri avatar) {
        this.avatar = avatar;
        isAvatarSet = true;
        return this;
    }

    public Uri getAvatar() {
        return avatar;
    }


}

package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.example.edunet.R;

public final class User extends Entity {
    private final String id;
    private final String name;
    private final String email;
    private final Uri avatar;
    private final String bio;

    public User(String id, String name, String email, Uri avatar, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.bio = bio;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Uri getAvatar() {
        return avatar;
    }

    public String getBio() {
        return bio;
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @DrawableRes
    @Override
    public int requireDefaultAvatar() {
        return R.drawable.ic_default_user;
    }

}

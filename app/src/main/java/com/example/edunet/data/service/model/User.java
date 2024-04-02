package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.DrawableRes;

import com.example.edunet.R;


public record User(String id, String name, Uri avatar, String bio) implements Entity {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Uri getAvatar() {
        return avatar;
    }

    @DrawableRes
    @Override
    public int requireDefaultAvatar() {
        return R.drawable.ic_default_user;
    }
}

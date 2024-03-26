package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.DrawableRes;

import com.example.edunet.R;
import com.example.edunet.common.util.UriUtils;


public record User(String id, String name, Uri avatar, String bio) implements Entity {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return UriUtils.safeToString(avatar);
    }

    @DrawableRes
    @Override
    public int requireDefaultAvatar() {
        return R.drawable.ic_default_user;
    }
}

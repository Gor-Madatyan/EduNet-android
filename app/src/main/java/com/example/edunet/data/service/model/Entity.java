package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.DrawableRes;

public interface Entity {
    String getName();
    Uri getAvatar();
    String getId();
    @DrawableRes int requireDefaultAvatar();
}

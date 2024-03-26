package com.example.edunet.data.service.model;

import androidx.annotation.DrawableRes;

public interface Entity {
    String getName();
    String getAvatar();
    @DrawableRes int requireDefaultAvatar();
}

package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Entity {
    public abstract String getName();
    public abstract Uri getAvatar();
    @NonNull
    public abstract String getId();
    public abstract @DrawableRes int requireDefaultAvatar();

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null | !(obj instanceof Entity)){
            return false;
        }
        return getId().equals(((Entity) obj).getId());
    }
}

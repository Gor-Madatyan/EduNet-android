package com.example.edunet.data.service;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edunet.data.service.exceptions.ServiceException;
import com.example.edunet.data.service.impl.AvatarServiceImpl;

import java.util.function.Consumer;

public interface AvatarService {
    AvatarService IMPL = new AvatarServiceImpl();

    void saveAvatarOfCurrentUser(@Nullable Uri photo,
                                 @Nullable String extension,
                                 @NonNull Consumer<Uri> onSuccess,
                                 @NonNull Consumer<Void> onEarlySuccess,
                                 @NonNull Consumer<ServiceException> onFailure);
}

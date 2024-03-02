package com.example.edunet.data.service.api.storage;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.example.edunet.data.service.exception.ServiceException;

public interface AvatarService {
    void saveAvatar(@NonNull Uri photo,
                    @NonNull Consumer<Uri> onSuccess,
                    @NonNull Consumer<ServiceException> onFailure);

    /**
     * @param photo the url of the photo
     * @return true if the implementation considers photo valid, otherwise false
     */
    boolean validateAvatar(@Nullable Uri photo);

}

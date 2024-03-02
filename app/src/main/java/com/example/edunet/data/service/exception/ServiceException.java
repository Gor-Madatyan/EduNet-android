package com.example.edunet.data.service.exception;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class ServiceException extends UserFriendlyException {
    public ServiceException(@StringRes int id, @Nullable Throwable cause) {
        super(id, cause);
    }

    public ServiceException(@StringRes int id) {
        super(id);
    }
}

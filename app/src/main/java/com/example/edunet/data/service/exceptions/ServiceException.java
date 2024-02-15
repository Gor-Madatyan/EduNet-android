package com.example.edunet.data.service.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ServiceException extends Exception {
    public ServiceException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public ServiceException(@NonNull String message) {
        super(message);
    }
}

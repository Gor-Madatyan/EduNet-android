package com.example.edunet.data.service.exception;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Objects;

public abstract class UserFriendlyException extends Exception {
    @StringRes
    private final int id;

    public UserFriendlyException(@StringRes int id) {
        this(id, null);
    }

    public UserFriendlyException(@StringRes int id, @Nullable Throwable cause) {
        super("Occurred error at " + id + " id", cause);
        this.id = id;
    }

    @NonNull
    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }
    public @StringRes int getId() {
        return id;
    }
}

package com.example.edunet.data.service.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;

public interface AccountService {
    final class InternalErrorMessages {
        public static final String CURRENT_USER_IS_NULL = "current user was null when it was not expected";
        private InternalErrorMessages() {
        }
    }

    @NonNull
    LiveData<User> observeCurrentUser();

    @Nullable
    User getCurrentUser();

    void updateCurrentUser(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean validateUserUpdate(@NonNull UserUpdateRequest request);

    void signOut();

}

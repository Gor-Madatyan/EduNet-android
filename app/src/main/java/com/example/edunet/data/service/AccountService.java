package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.common.Paginator;

public interface AccountService {
    final class InternalErrorMessages {
        public static final String CURRENT_USER_IS_NULL = "current user was null when it was not expected";

        private InternalErrorMessages() {
        }
    }
    void onSignIn();

    @Nullable
    String getUid();

    void getUserById(@NonNull String uid, @NonNull Consumer<User> onSuccess, @NonNull Consumer<ServiceException> onFailure);

    boolean isUserAvailable();

    @NonNull
    LiveData<User> observeCurrentUser();

    @Nullable
    User getCurrentUser();

    Paginator<Pair<String, User>> getUserArrayPaginator(String[] uids, int limit);

    void updateCurrentUser(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean validateUserUpdate(@NonNull UserUpdateRequest request);

    void signOut();
}

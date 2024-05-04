package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.EmailCredential;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.paginator.Paginator;

public interface AccountService {
    final class InternalErrorMessages {
        public static final String CURRENT_USER_IS_NULL = "current user was null when it was not expected";

        private InternalErrorMessages() {
        }
    }

    void signInWithEmailAddress(@NonNull String email, @NonNull String password, @NonNull Consumer<ServiceException> onResult);

    void signInWithGoogleIdToken(@NonNull String idToken, @NonNull Consumer<ServiceException> onResult);

    void signUpWithEmailAddress(@NonNull EmailCredential emailCredential, @NonNull Consumer<ServiceException> onResult);

    void sendEmailVerification(@NonNull Consumer<ServiceException> onResult);

    @Nullable
    String getUid();

    void getUserById(@NonNull String uid, @NonNull Consumer<User> onSuccess, @NonNull Consumer<ServiceException> onFailure);

    boolean isUserAvailable();

    boolean isCurrentUserEmailVerified();

    @NonNull
    LiveData<User> observeCurrentUser();

    @NonNull
    LiveData<User> observeUser(@NonNull LifecycleOwner owner, @NonNull String uid);

    @Nullable
    User getCurrentUser();

    Paginator<User> getUserArrayPaginator(String[] uids, int limit);

    void updateCurrentUser(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean isUserUpdateInvalid(@NonNull UserUpdateRequest request);

    boolean validateEmail(@NonNull String email);

    boolean validatePassword(@NonNull String password);

    boolean validateEmailCredentials(@NonNull EmailCredential emailCredential);

    void signOut();
}

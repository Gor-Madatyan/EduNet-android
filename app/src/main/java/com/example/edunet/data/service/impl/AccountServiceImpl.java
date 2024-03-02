package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.FirebaseTypesConversionsUtils.FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest;
import static com.example.edunet.data.service.util.firebase.FirebaseTypesConversionsUtils.userFromFireBaseUser;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.R;
import com.example.edunet.data.service.api.AccountService;
import com.example.edunet.data.service.api.StorageService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccountServiceImpl implements AccountService {
    private final FirebaseAuth auth;
    private final StorageService storageService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();


    @Inject
    AccountServiceImpl(FirebaseAuth auth, StorageService storageService) {
        this.auth = auth;
        this.storageService = storageService;

        auth.addAuthStateListener(firebaseAuth ->
                currentUser.setValue(userFromFireBaseUser(auth.getCurrentUser()))
        );
    }

    @Override
    @NonNull
    public LiveData<User> observeCurrentUser() {
        return currentUser;
    }

    @Override
    @Nullable
    public User getCurrentUser() {
        return userFromFireBaseUser(auth.getCurrentUser());
    }

    @Override
    public void updateCurrentUser(@NonNull UserUpdateRequest abstractRequest, @NonNull Consumer<ServiceException> onResult) {
        if (!validateUserUpdate(abstractRequest)) {
            onResult.accept(new ServiceException(R.string.error_invalid_profile_update_request));
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        user.updateProfile(FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest(abstractRequest))
                .addOnSuccessListener(v -> refreshCurrentUser())
                .addOnCompleteListener(r -> {
                            Exception e = r.getException();
                            onResult.accept(e == null ? null : new ServiceException(R.string.error_profile_update, e));
                        }
                );


    }

    @Override
    public boolean validateUserUpdate(@NonNull UserUpdateRequest request) {
        Objects.requireNonNull(request);
        String name = request.getName();
        Uri photo = request.getAvatar();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;

        if (((request.isNameSet() || user.getDisplayName() == null)) && name == null)
            return false;

        if (request.isAvatarSet() && photo != null && !storageService.isUrlDomestic(photo.toString()))
            return false;

        if (name != null) request.setName(name = name.trim());

        return name == null || !name.isEmpty();
    }

    @Override
    public void signOut() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        if (user.isAnonymous()) user.delete();
        auth.signOut();
    }

    private void refreshCurrentUser() {
        currentUser.setValue(getCurrentUser());
    }

}

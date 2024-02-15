package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.FirebaseTypesConversionsUtils.fireBaseUserChangeRequestFromAbstractUserChangeRequest;
import static com.example.edunet.data.service.util.firebase.FirebaseTypesConversionsUtils.userFromFireBaseUser;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exceptions.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Objects;
import java.util.function.Consumer;

public final class AccountServiceImpl implements AccountService {
    private final static FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    {
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
    public void signOut() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        if (user.isAnonymous()) user.delete();
        auth.signOut();
    }

    @Override
    public User getCurrentUser() {
        return userFromFireBaseUser(auth.getCurrentUser());
    }

    @Override
    public boolean isUserPhotoDomestic() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        Uri photo = user.getPhotoUrl();

        if (photo == null) return true;

        for (UserInfo provider : user.getProviderData())

            if (!provider.getProviderId().equals(FirebaseAuthProvider.PROVIDER_ID) &&
                    Objects.equals(photo, provider.getPhotoUrl()))
                return false;


        return true;
    }

    @Override
    public void reloadUserInfo(@NonNull Consumer<ServiceException> onResult) {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;

        user.reload()
                .addOnCompleteListener(r -> {
                    Exception e = r.getException();
                    onResult.accept(e == null ? null : new ServiceException("cant fetch user", e));
                })
                .addOnSuccessListener(v -> refreshCurrentUser());

    }

    private boolean validateUserChange(UserChangeRequest request) {
        String name = request.getName();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        if (((request.isNameSet() || user.getDisplayName() == null)) && name == null)
            return false;
        if (name != null) request.setName(name = name.trim());

        return name == null || !name.isEmpty();
    }

    @Override
    public void updateCurrentUser(@NonNull UserChangeRequest abstractRequest, @NonNull Consumer<ServiceException> onResult) {
        if (!validateUserChange(abstractRequest)) {
            onResult.accept(new ServiceException("Invalid user update request"));
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        user.updateProfile(fireBaseUserChangeRequestFromAbstractUserChangeRequest(abstractRequest))
                .addOnSuccessListener(v -> refreshCurrentUser())
                .addOnCompleteListener(r -> {
                            Exception e = r.getException();
                            onResult.accept(e == null ? null : new ServiceException("cant update user", e));
                        }
                );


    }


    private void refreshCurrentUser() {
        currentUser.setValue(userFromFireBaseUser(auth.getCurrentUser()));
    }

}

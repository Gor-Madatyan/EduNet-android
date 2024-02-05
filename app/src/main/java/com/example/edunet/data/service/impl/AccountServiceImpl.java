package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.FirebaseTypesConversionsUtils.fireBaseUserChangeRequestFromAbstractUserChangeRequest;
import static com.example.edunet.data.service.util.FirebaseTypesConversionsUtils.userFromFireBaseUser;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;
import com.google.firebase.auth.FirebaseAuth;

import java.util.function.Consumer;

public final class AccountServiceImpl implements AccountService {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    {
        auth.addAuthStateListener(firebaseAuth ->
                currentUser.setValue(userFromFireBaseUser(firebaseAuth.getCurrentUser()))
        );
    }

    @Override
    @NonNull
    public LiveData<User> observeCurrentUser() {
        return currentUser;
    }

    @Override
    public void signOut() {
        var user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        if (user.isAnonymous()) user.delete();
        auth.signOut();
    }

    @Override
    public void updateCurrentUser(@NonNull UserChangeRequest updateUserInfo, Consumer<Exception> onFailure) {
        var user = auth.getCurrentUser();
        assert user != null : ErrorMessages.CURRENT_USER_IS_NULL;
        user.updateProfile(fireBaseUserChangeRequestFromAbstractUserChangeRequest(updateUserInfo))
                .addOnSuccessListener(v -> refreshCurrentUser())
                .addOnFailureListener(onFailure::accept);

    }

    private void refreshCurrentUser() {
        currentUser.setValue(userFromFireBaseUser(auth.getCurrentUser()));
    }

}

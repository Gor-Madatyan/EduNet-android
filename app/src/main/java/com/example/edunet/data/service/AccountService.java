package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;

import java.util.function.Consumer;

public interface AccountService {
    final class ErrorMessages{
        public static final String CURRENT_USER_IS_NULL="current user is null when it was not expected";
    private ErrorMessages(){}
    }
    AccountService IMPL = new AccountServiceImpl();

    @NonNull
    LiveData<User> observeCurrentUser();
    void signOut();
    void updateCurrentUser(UserChangeRequest request, Consumer<Exception> onFailure);
}

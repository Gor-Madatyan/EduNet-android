package com.example.edunet.ui.screen.auth.signup;

import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.UserFriendlyException;
import com.example.edunet.data.service.model.EmailCredential;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpViewModel extends ViewModel {
    private final AccountService accountService;

    @Inject
    SignUpViewModel(AccountService accountService) {
        this.accountService = accountService;
    }

    void signUpWithEmailAddress(EmailCredential credentials, Consumer<UserFriendlyException> onResult) {
        accountService.signUpWithEmailAddress(credentials, onResult::accept);
    }

}
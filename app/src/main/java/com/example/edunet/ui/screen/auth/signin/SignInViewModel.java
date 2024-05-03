package com.example.edunet.ui.screen.auth.signin;

import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.UserFriendlyException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignInViewModel extends ViewModel {
    private final AccountService accountService;

     @Inject
     SignInViewModel(AccountService accountService){
         this.accountService = accountService;
     }

     public void signInWithEmailAndPassword(String email, String password, Consumer<UserFriendlyException> onResult){
         accountService.signInWithEmailAddress(email, password, onResult::accept);
     }

     public void signInWithGoogleId(String idToken, Consumer<UserFriendlyException> onResult){
         accountService.signInWithGoogleIdToken(idToken, onResult::accept);
     }
}
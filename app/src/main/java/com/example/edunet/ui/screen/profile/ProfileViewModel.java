package com.example.edunet.ui.screen.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;

public class ProfileViewModel extends ViewModel {
    private final static AccountService accountService = AccountService.IMPL;
    private final MediatorLiveData<ProfileUiState> _uiState = new MediatorLiveData<>();
    final LiveData<ProfileUiState> uiState = _uiState;

    {
        _uiState.addSource(accountService.observeCurrentUser(),
                user -> {
                    if (user != null) _uiState.setValue(new ProfileUiState(user.name(), user.photo()));
                });
    }

    void signOut() {
        accountService.signOut();
    }

}

record ProfileUiState(String userName, Uri userPhoto) {
}
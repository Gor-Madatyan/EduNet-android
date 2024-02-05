package com.example.edunet.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;

public class ProfileViewModel extends ViewModel {
    private final MediatorLiveData<ProfileUiState> _uiState = new MediatorLiveData<>();
    final LiveData<ProfileUiState> uiState = _uiState;

    {
        _uiState.addSource(AccountService.IMPL.observeCurrentUser(),
                user -> {
                    if (user != null) _uiState.setValue(new ProfileUiState(user.name(), user.photo()));
                });
    }

    void signOut() {
        AccountService.IMPL.signOut();
    }

}

record ProfileUiState(String userName, Uri userPhoto) {
}
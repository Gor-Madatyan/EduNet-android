package com.example.edunet.ui.screen.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final AccountService accountService;
    private final MediatorLiveData<UiState> _uiState = new MediatorLiveData<>();
    final LiveData<UiState> uiState = _uiState;

    @Inject
    ProfileViewModel(AccountService accountService) {
        this.accountService = accountService;

        _uiState.addSource(accountService.observeCurrentUser(),
                user -> {
                    if (user != null)
                        _uiState.setValue(new UiState(user));
                });
    }

    void signOut() {
        accountService.signOut();
    }

}

record UiState(User user) {
}
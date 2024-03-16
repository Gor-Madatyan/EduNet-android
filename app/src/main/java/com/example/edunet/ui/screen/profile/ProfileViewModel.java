package com.example.edunet.ui.screen.profile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.User;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private static final String TAG = ProfileViewModel.class.getSimpleName();
    private final AccountService accountService;
    private final CommunityService communityService;
    private final MediatorLiveData<UiState> _uiState = new MediatorLiveData<>();
    final LiveData<UiState> uiState = _uiState;

    @SuppressWarnings("unchecked")
    @Inject
    ProfileViewModel(AccountService accountService, CommunityService communityService) {
        this.accountService = accountService;
        this.communityService = communityService;

        _uiState.addSource(accountService.observeCurrentUser(),
                user -> {
                    if (user != null) {
                        UiState currentUiState = _uiState.getValue();
                        Pair<String, Community>[] ownedCommunities = currentUiState == null ? new Pair[0] : currentUiState.ownedCommunities();
                        _uiState.setValue(new UiState(user, ownedCommunities));
                    }
                });
    }

    void observeOwnedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeOwnedCommunities(owner,
                Objects.requireNonNull(accountService.getUid()),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG,e.getMessage());
                        return;
                    }
                    User user = accountService.getCurrentUser();
                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

                    _uiState.setValue(new UiState(user, communities));
                }
        );
    }


    void signOut() {
        accountService.signOut();
    }

}

record UiState(@NonNull User user, @NonNull Pair<String, Community>[] ownedCommunities) {

}
package com.example.edunet.ui.screen.profile;

import android.net.Uri;

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

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
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
                    if (user != null)
                        _uiState.setValue(UiState.fromUser(user, new Pair[0]));
                });
    }

    @SuppressWarnings("unchecked")
    void observeCommunities(@NonNull LifecycleOwner owner) {
        accountService.observeCurrentUser().observe(owner,
                user -> {
                    if (user == null) return;

                    communityService.loadCommunities(
                            Arrays.asList(user.ownedCommunities()),
                            communities -> _uiState.setValue(UiState.fromUser(user, communities.toArray(new Pair[0]))),

                            e -> _uiState.setValue(UiState.fromUser(user, new Pair[0]))
                    );

                }
        );
    }


    void signOut() {
        accountService.signOut();
    }

}

record UiState(@NonNull Uri avatar,
               @NonNull String name,
               @NonNull String bio,
               @NonNull Pair<String, Community>[] ownedCommunities) {
    static UiState fromUser(@NonNull User user, @NonNull Pair<String, Community>[] ownedCommunities) {
        return new UiState(
                user.photo(),
                user.name(),
                user.bio(),
                ownedCommunities);
    }
}
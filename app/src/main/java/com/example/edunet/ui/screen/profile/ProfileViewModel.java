package com.example.edunet.ui.screen.profile;

import android.util.Log;

import androidx.annotation.NonNull;
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

    @Inject
    ProfileViewModel(AccountService accountService, CommunityService communityService) {
        this.accountService = accountService;
        this.communityService = communityService;

        _uiState.addSource(accountService.observeCurrentUser(),
                user -> {
                    if (user != null) {
                        UiState currentUiState = _uiState.getValue();
                        Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                        Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                        Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();


                        _uiState.setValue(new UiState(user, ownedCommunities, adminedCommunities, participatedCommunities));
                    }
                });
    }

    private void observeOwnedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeOwnedCommunities(owner,
                Objects.requireNonNull(accountService.getUid()),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = currentUiState == null ? null : currentUiState.user();
                    Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                    Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, communities, adminedCommunities, participatedCommunities));
                }
        );
    }

    private void observeAdminedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeAdminedCommunities(owner,
                Objects.requireNonNull(accountService.getUid()),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = accountService.getCurrentUser();
                    Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                    Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, ownedCommunities, communities, participatedCommunities));
                }
        );
    }

    private void observeParticipatedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeParticipatedCommunities(owner,
                Objects.requireNonNull(accountService.getUid()),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = accountService.getCurrentUser();
                    Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                    Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, ownedCommunities, adminedCommunities, communities));
                }
        );
    }

    void observeAttachedCommunities(@NonNull LifecycleOwner owner) {
        observeOwnedCommunities(owner);
        observeAdminedCommunities(owner);
        observeParticipatedCommunities(owner);
    }


    void signOut() {
        accountService.signOut();
    }

}

record UiState(@NonNull User user,
               @NonNull Community[] ownedCommunities,
               @NonNull Community[] adminedCommunities,
               @NonNull Community[] participatedCommunities) {

}
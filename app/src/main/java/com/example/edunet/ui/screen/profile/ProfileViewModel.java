package com.example.edunet.ui.screen.profile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private String uid;

    @Inject
    ProfileViewModel(AccountService accountService, CommunityService communityService) {
        this.accountService = accountService;
        this.communityService = communityService;
    }

    void setUser(@Nullable String uid, @NonNull LifecycleOwner owner) {
        this.uid = uid == null ? Objects.requireNonNull(accountService.getUid()) : uid;
        LiveData<User> liveData = isUserCurrent() ? accountService.observeCurrentUser() : accountService.observeUser(owner, this.uid);

        _uiState.removeSource(liveData);
        _uiState.addSource(liveData,
                user -> {
                    if (user != null) {
                        UiState currentUiState = _uiState.getValue();
                        Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                        Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                        Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();
                        Community[] graduatedCommunities = currentUiState == null ? new Community[0] : currentUiState.graduatedCommunities();


                        _uiState.setValue(new UiState(user, ownedCommunities, adminedCommunities, participatedCommunities, graduatedCommunities));
                    }
                });
    }

    boolean isUserCurrent() {
        return Objects.equals(uid, accountService.getUid());
    }

    boolean isCurrentUserEmailVerified() {
        return accountService.isCurrentUserEmailVerified();
    }

    private void observeOwnedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeOwnedCommunities(owner,
                Objects.requireNonNull(uid),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = currentUiState == null ? null : currentUiState.user();
                    Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                    Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();
                    Community[] graduatedCommunities = currentUiState == null ? new Community[0] : currentUiState.graduatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, communities, adminedCommunities, participatedCommunities, graduatedCommunities));
                }
        );
    }

    private void observeAdminedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeAdminedCommunities(owner,
                Objects.requireNonNull(uid),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = currentUiState == null ? null : currentUiState.user();
                    Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                    Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();
                    Community[] graduatedCommunities = currentUiState == null ? new Community[0] : currentUiState.graduatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, ownedCommunities, communities, participatedCommunities, graduatedCommunities));
                }
        );
    }

    private void observeParticipatedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeParticipatedCommunities(owner,
                Objects.requireNonNull(uid),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = currentUiState == null ? null : currentUiState.user();
                    Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                    Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                    Community[] graduatedCommunities = currentUiState == null ? new Community[0] : currentUiState.graduatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, ownedCommunities, adminedCommunities, communities, graduatedCommunities));
                }
        );
    }

    private void observeGraduatedCommunities(@NonNull LifecycleOwner owner) {
        communityService.observeGraduatedCommunities(owner,
                Objects.requireNonNull(uid),
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e.toString());
                        return;
                    }
                    UiState currentUiState = _uiState.getValue();
                    User user = currentUiState == null ? null : currentUiState.user();
                    Community[] ownedCommunities = currentUiState == null ? new Community[0] : currentUiState.ownedCommunities();
                    Community[] adminedCommunities = currentUiState == null ? new Community[0] : currentUiState.adminedCommunities();
                    Community[] participatedCommunities = currentUiState == null ? new Community[0] : currentUiState.participatedCommunities();

                    assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
                    _uiState.setValue(new UiState(user, ownedCommunities, adminedCommunities, participatedCommunities, communities));
                }
        );
    }

    void observeAttachedCommunities(@NonNull LifecycleOwner owner) {
        observeOwnedCommunities(owner);
        observeAdminedCommunities(owner);
        observeParticipatedCommunities(owner);
        observeGraduatedCommunities(owner);
    }


    void signOut() {
        accountService.signOut();
    }

}

record UiState(@NonNull User user,
               @NonNull Community[] ownedCommunities,
               @NonNull Community[] adminedCommunities,
               @NonNull Community[] participatedCommunities,
               @NonNull Community[] graduatedCommunities) {

}
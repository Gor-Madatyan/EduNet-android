package com.example.edunet.ui.util.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.NotificationsService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.exception.UserFriendlyException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.Role;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommunityViewModel extends ViewModel {
    private static final String TAG = CommunityViewModel.class.getSimpleName();
    private final NotificationsService notificationsService;
    private final CommunityService communityService;
    private final AccountService accountService;
    private final MutableLiveData<UiState> _uiState = new MutableLiveData<>();
    private String communityId;
    private Role role;
    private boolean isSuperCommunityObserved = false;
    public final LiveData<UiState> uiState = _uiState;

    public record UiState(
            Error error,
            Community community,
            Community superCommunity,
            Community[] subCommunities,
            Role role,
            boolean isCurrentUserRequestedAdminPermissions,
            boolean isCurrentUserRequestedParticipantPermissions) {
    }

    public record Error(@StringRes int messageId) {
    }

    @Inject
    CommunityViewModel(NotificationsService notificationsService, CommunityService communityService, AccountService accountService) {
        this.notificationsService = notificationsService;
        this.communityService = communityService;
        this.accountService = accountService;
    }

    private void onError(ServiceException exception) {
        _uiState.setValue(new UiState(
                new Error(exception.getId()),
                null,
                null,
                new Community[0],
                Role.GUEST,
                false,
                false)
        );
        Log.w(TAG, exception);
    }

    public void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid) {
        communityId = cid;
        communityService.observeCommunity(lifecycleOwner, cid,
                (community, e) -> {
                    if (e != null) {
                        onError(e);
                        return;
                    }
                    String uid = accountService.getUid();
                    assert uid != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

                    if (community.getAncestor() != null && !isSuperCommunityObserved) {
                        isSuperCommunityObserved = true;
                        observeSuperCommunity(lifecycleOwner, community.getAncestor());
                    }

                    role = !accountService.isCurrentUserEmailVerified() ? Role.UNVERIFIED_GUEST : community.getUserRole(uid);
                    _uiState.setValue(new UiState(
                            null,
                            community,
                            uiState.getValue() == null ? null : uiState.getValue().superCommunity(),
                            uiState.getValue() == null ? new Community[0] : uiState.getValue().subCommunities(),
                            role,
                            community.getAdminsQueue().contains(uid),
                            community.getParticipantsQueue().contains(uid)));
                }
        );
        observeSubCommunities(lifecycleOwner, cid);
    }


    private void observeSubCommunities(@NonNull LifecycleOwner owner, @NonNull String id) {
        communityService.observeSubCommunities(owner, id,
                (e, subCommunities) -> {
                    if (e != null) {
                        onError(e);
                        return;
                    }
                    UiState currentUiState = uiState.getValue();
                    Community community = currentUiState == null ? null : currentUiState.community();
                    Community superCommunity = currentUiState == null ? null : currentUiState.superCommunity();

                    Role role = currentUiState == null ? Role.GUEST : currentUiState.role();
                    boolean isCurrentUserRequestedAdminPermissions = currentUiState != null && currentUiState.isCurrentUserRequestedAdminPermissions();
                    boolean isCurrentUserRequestedParticipantPermissions = currentUiState != null && currentUiState.isCurrentUserRequestedParticipantPermissions();


                    _uiState.setValue(
                            new UiState(null,
                                    community,
                                    superCommunity,
                                    subCommunities,
                                    role,
                                    isCurrentUserRequestedAdminPermissions,
                                    isCurrentUserRequestedParticipantPermissions)
                    );
                });
    }

    private void observeSuperCommunity(@NonNull LifecycleOwner owner, @NonNull String id) {
        communityService.observeCommunity(owner, id,
                (superCommunity, e) -> {
                    if (e != null) {
                        onError(e);
                        return;
                    }
                    UiState currentUiState = uiState.getValue();
                    Community community = currentUiState == null ? null : currentUiState.community();
                    Community[] subCommunities = currentUiState == null ? null : currentUiState.subCommunities();
                    Role role = currentUiState == null ? Role.GUEST : currentUiState.role();
                    boolean isCurrentUserRequestedAdminPermissions = currentUiState != null && currentUiState.isCurrentUserRequestedAdminPermissions();
                    boolean isCurrentUserRequestedParticipantPermissions = currentUiState != null && currentUiState.isCurrentUserRequestedParticipantPermissions();


                    _uiState.setValue(
                            new UiState(null,
                                    community,
                                    superCommunity,
                                    subCommunities,
                                    role,
                                    isCurrentUserRequestedAdminPermissions,
                                    isCurrentUserRequestedParticipantPermissions)
                    );
                });
    }

    public void manageCommunitySubscription(boolean subscribe, Consumer<UserFriendlyException> onResult) {
        notificationsService.manageCommunitySubscription(communityId, subscribe, onResult::accept);
    }

    @Nullable
    public Role getRole() {
        return role;
    }
}



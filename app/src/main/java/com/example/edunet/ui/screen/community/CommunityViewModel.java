package com.example.edunet.ui.screen.community;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Community;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommunityViewModel extends ViewModel {
    private static final String TAG = CommunityViewModel.class.getSimpleName();
    private final CommunityService communityService;
    private final AccountService accountService;
    private final MutableLiveData<UiState> _uiState = new MutableLiveData<>();
    final LiveData<UiState> uiState = _uiState;

    @Inject
    CommunityViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    @SuppressWarnings("unchecked")
    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id) {
        communityService.observeCommunity(lifecycleOwner, id,
                (community, exception) -> {
                    if (exception != null) {
                        _uiState.setValue(new UiState(
                                new Error(exception.getId()),
                                null,
                                new Pair[0],
                                false)
                        );
                        Log.w(TAG, exception.toString());
                        return;
                    }
                    String uid = accountService.getUid();
                    assert uid != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

                    _uiState.setValue(new UiState(
                            null,
                            community,
                            uiState.getValue() == null ? new Pair[0] : uiState.getValue().subCommunities(),
                            uid.equals(community.getOwnerId())));
                }
        );
        observeSubCommunities(lifecycleOwner, id);
    }

    @SuppressWarnings("unchecked")
    private void observeSubCommunities(@NonNull LifecycleOwner owner, @NonNull String id) {
        communityService.observeSubCommunities(owner, id,
                (e, subCommunities) -> {
                    UiState currentUiState = uiState.getValue();
                    Community community = currentUiState == null ? null : currentUiState.community();
                    boolean isCurrentUserOwner = currentUiState != null && currentUiState.isCurrentUserOwner();

                    if (e != null) {
                        _uiState.setValue(new UiState(
                                new Error(e.getId()),
                                community,
                                new Pair[0],
                                isCurrentUserOwner)
                        );
                        Log.e(TAG, e.toString());
                        return;
                    }

                    _uiState.setValue(
                            new UiState(null,
                                    community,
                                    subCommunities,
                                    isCurrentUserOwner)
                    );
                });
    }
}


record UiState(
        Error error,
        Community community,
        Pair<String, Community>[] subCommunities,
        boolean isCurrentUserOwner) {
}

record Error(@StringRes int messageId) {
}
package com.example.edunet.ui.screen.community;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
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

    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id) {
        communityService.observeCommunity(lifecycleOwner, id,
                (community, exception) -> {
                    if (exception != null) {
                        _uiState.setValue(new UiState(
                                new Error(exception.getId()),
                                null,
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
                            uid.equals(community.getOwnerId())));
                }
        );
    }
}


record UiState(
        Error error,
        Community community,
        boolean isCurrentUserOwner) {
}

record Error(@StringRes int messageId) {
}
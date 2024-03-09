package com.example.edunet.ui.screen.community;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Community;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommunityViewModel extends ViewModel {
    private static final String TAG = CommunityViewModel.class.getSimpleName();
    private final CommunityService communityService;
    private final MutableLiveData<UiState> _uiState = new MediatorLiveData<>();
    final LiveData<UiState> uiState = _uiState;

    @Inject
    CommunityViewModel(CommunityService communityService) {
        this.communityService = communityService;
    }

    void setCommunity(@NonNull Fragment fragment, @NonNull String id) {
        communityService.observeCommunity(fragment.getViewLifecycleOwner(), id,
                (community, exception) -> {
                    if(exception != null)
                        Log.e(TAG,exception.toString());
                    else _uiState.setValue(new UiState(community));
                }
        );
    }
}


record UiState(Community community) {
}
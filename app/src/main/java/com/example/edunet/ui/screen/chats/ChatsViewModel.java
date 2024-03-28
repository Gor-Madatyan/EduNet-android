package com.example.edunet.ui.screen.chats;

import android.util.Log;

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
public class ChatsViewModel extends ViewModel {
    private static final String TAG = ChatsViewModel.class.getSimpleName();
    private final CommunityService communityService;
    private final AccountService accountService;
    private final MutableLiveData<Pair<String, Community>[]> _dataset = new MutableLiveData<>();
    final LiveData<Pair<String, Community>[]> dataset = _dataset;

    @Inject
    ChatsViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    void setListener(LifecycleOwner owner) {
        String uid = accountService.getUid();
        assert uid != null;

        communityService.observeAttachedCommunities(owner, uid,
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e);
                        return;
                    }

                    _dataset.setValue(communities);
                }
        );
    }
}

package com.example.edunet.ui.screen.adminpanel.adminrequests;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.common.Paginator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AdminRequestsViewModel extends ViewModel {
    private static final String TAG = AdminRequestsViewModel.class.getSimpleName();
    private static final int PAGINATOR_LIMIT = 20;
    private String communityId;
    private final CommunityService communityService;
    private final AccountService accountService;

    private final MutableLiveData<Paginator<Pair<String, User>>> _paginator = new MutableLiveData<>();
    final LiveData<Paginator<Pair<String, User>>> paginator = _paginator;

    @Inject
    AdminRequestsViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    void setCommunity(String communityId) {
        this.communityId = communityId;

        communityService.getCommunity(communityId,
                community ->
                        _paginator.setValue(accountService.getUserArrayPaginator(
                                community.getAdminsQueue().toArray(new String[0]),
                                PAGINATOR_LIMIT
                        )),
                e ->
                        Log.e(TAG, e.toString())
        );
    }

    void acceptAdmin(String uid, Consumer<Exception> onResult) {
        communityService.setAdminPermissions(communityId, uid, onResult::accept);
    }

    void deleteAdminRequest(String uid, Consumer<Exception> onResult) {
        communityService.deleteAdminRequest(communityId, uid, onResult::accept);
    }
}
package com.example.edunet.ui.screen.adminpanel.requests;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.paginator.Paginator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestsViewModel extends ViewModel {
    private static final String TAG = RequestsViewModel.class.getSimpleName();
    private static final int PAGINATOR_LIMIT = 20;
    private final CommunityService communityService;
    private final AccountService accountService;
    LiveData<Paginator<User>> paginator;

    @Inject
    RequestsViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    void setCommunity(@NonNull String communityId, @NonNull Role role) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;
        MutableLiveData<Paginator<User>> _paginator = new MutableLiveData<>();
        paginator = _paginator;

        communityService.getCommunity(communityId,
                community ->
                        _paginator.setValue(accountService.getUserArrayPaginator(
                                (role == Role.ADMIN ? community.getAdminsQueue() : community.getParticipantsQueue()).toArray(new String[0]),
                                PAGINATOR_LIMIT
                        )),
                e ->
                        Log.e(TAG, e.toString())
        );
    }

}
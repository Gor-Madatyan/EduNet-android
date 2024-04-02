package com.example.edunet.ui.screen.adminpanel.members;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.common.Paginator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MembersViewModel extends ViewModel  {
    private static final String TAG = MembersViewModel.class.getSimpleName();
    private static final int PAGINATOR_LIMIT = 20;
    private String communityId;
    private Role role;
    private final CommunityService communityService;
    private final AccountService accountService;
    LiveData<Paginator<User>> paginator;

    @Inject
    MembersViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

     void setCommunity(@NonNull String communityId, @NonNull Role role) {
        assert role != Role.OWNER && role != Role.GUEST;
        MutableLiveData<Paginator<User>> _paginator = new MutableLiveData<>();
        paginator = _paginator;
        this.communityId = communityId;
        this.role = role;

        communityService.getCommunity(communityId,
                community ->
                        _paginator.setValue(accountService.getUserArrayPaginator(
                                (role == Role.ADMIN ? community.getAdmins() : community.getParticipants()).toArray(new String[0]),
                                PAGINATOR_LIMIT
                        )),
                e ->
                        Log.e(TAG, e.toString())
        );
    }

     void delete(String uid, Consumer<Exception> onResult) {
        if(role == Role.ADMIN) communityService.deleteAdmin(communityId, uid, onResult::accept);
        else communityService.deleteParticipant(communityId, uid, onResult::accept);
    }
}

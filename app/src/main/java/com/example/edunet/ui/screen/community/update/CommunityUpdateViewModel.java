package com.example.edunet.ui.screen.community.update;


import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.task.community.CommunityTaskManager;
import com.example.edunet.data.service.util.work.WorkUtils;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommunityUpdateViewModel extends ViewModel {
    private final CommunityTaskManager communityTaskManager;
    private final CommunityService communityService;
    private String communityId;
    private Community community;
    private final MutableLiveData<Error> _error = new MutableLiveData<>();
    private final MutableLiveData<Uri> _avatar = new MutableLiveData<>();
    LiveData<Uri> avatar = _avatar;
    LiveData<Error> error = _error;

    @Inject
    CommunityUpdateViewModel(CommunityService communityService,CommunityTaskManager communityTaskManager) {
        this.communityService = communityService;
        this.communityTaskManager = communityTaskManager;
    }

    void updateCommunity(@NonNull Context context, @NonNull String name, @NonNull String description) {
        assert communityId != null;
        CommunityUpdateRequest request = new CommunityUpdateRequest(communityId).setName(name).setDescription(description);
        Uri avatar = _avatar.getValue();
        if(!Objects.equals(getInitialAvatar(), avatar)) request.setAvatar(avatar);

        if (!communityService.validateCommunityUpdateRequest(request)) {
            _error.setValue(new Error(R.string.error_invalid_community_update_request));
            return;
        }

        WorkUtils.observe(context.getApplicationContext(),communityTaskManager.startCommunityUpdateTask(request),R.string.error_cant_update_community);

        _error.setValue(null);

    }

    void setAvatar(@Nullable Uri avatar) {
        _avatar.setValue(avatar);
    }

    void setCommunity(@NonNull String communityId,@NonNull Community community) {
        this.communityId = communityId;
        this.community = community;
        String avatar = community.getAvatar();

        _avatar.setValue(avatar == null ? null : Uri.parse(avatar));
    }

    Uri getInitialAvatar(){
       String avatar = community.getAvatar();
       return avatar == null ? null : Uri.parse(avatar);
    }

}

record Error(@StringRes int id) {
}
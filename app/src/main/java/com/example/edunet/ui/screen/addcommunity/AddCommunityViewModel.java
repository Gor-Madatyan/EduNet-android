package com.example.edunet.ui.screen.addcommunity;

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
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.task.community.CommunityTaskManager;
import com.example.edunet.data.service.util.work.WorkUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddCommunityViewModel extends ViewModel {

    private final MutableLiveData<Uri> _avatar = new MutableLiveData<>();
    private final MutableLiveData<Error> _error = new MutableLiveData<>();
    final LiveData<Error> error = _error;

    final LiveData<Uri> avatar = _avatar;
    private final CommunityTaskManager communityTaskManager;
    private final CommunityService communityService;

    @Inject
    AddCommunityViewModel(CommunityTaskManager communityTaskManager, CommunityService communityService) {
        this.communityTaskManager = communityTaskManager;
        this.communityService = communityService;
    }


    void createCommunity(@NonNull String name, @NonNull String description, @Nullable String ancestor, @NonNull Context context) {
        Uri avatar = this.avatar.getValue();

        CommunityCreateRequest request = new CommunityCreateRequest()
                .setAvatar(avatar)
                .setName(name)
                .setDescription(description)
                .setAncestor(ancestor);

        if (communityService.isCommunityCreateRequestInvalid(request)) {
            _error.setValue(new Error(R.string.error_invalid_community_create_request));
            return;
        }

        WorkUtils.observe(context.getApplicationContext(),communityTaskManager.startCommunityCreateTask(request),R.string.error_cant_create_community);
        _error.setValue(null);

    }

    void setAvatar(@NonNull Uri avatar) {
        _avatar.setValue(avatar);
    }

}

record Error(@StringRes int messageId) {
}
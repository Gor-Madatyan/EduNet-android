package com.example.edunet.ui.screen.addcommunity;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkInfo;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.task.community.CommunityTaskManager;

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


    void createCommunity(@NonNull String name, @NonNull String description, @NonNull Context context) {
        Uri avatar = this.avatar.getValue();

        CommunityCreateRequest request = new CommunityCreateRequest()
                .setAvatar(avatar)
                .setName(name)
                .setDescription(description);

        if (!communityService.validateCommunityCreateRequest(request)) {
            _error.setValue(new Error(R.string.error_invalid_community_create_request));
            return;
        }

        LiveData<WorkInfo> workInfoLiveData = communityTaskManager.startCommunityCreateTask(request);
        _error.setValue(null);

        Observer<WorkInfo> observer = new Observer<>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                WorkInfo.State state = workInfo.getState();

                if (state.isFinished()) {
                    workInfoLiveData.removeObserver(this);
                    if (state == WorkInfo.State.FAILED)
                        Toast.makeText(context, R.string.error_cant_create_community, Toast.LENGTH_SHORT).show();
                }
            }
        };

        workInfoLiveData.observeForever(observer);

    }

    void setAvatar(@NonNull Uri avatar) {
        _avatar.setValue(avatar);
    }

}

record Error(@StringRes int messageId) {
}
package com.example.edunet.ui.screen.profile.update;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkInfo;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.task.account.ProfileTaskManager;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ProfileUpdateViewModel extends ViewModel {
    private final AccountService accountService;
    private final ProfileTaskManager profileTaskManager;
    private final MutableLiveData<Uri> _avatar = new MutableLiveData<>();
    final LiveData<Uri> avatar = _avatar;
    private final MutableLiveData<Error> _error = new MutableLiveData<>();
    final LiveData<Error> error = _error;

    @Inject
    ProfileUpdateViewModel(AccountService accountService,
                           ProfileTaskManager profileTaskManager) {
        this.profileTaskManager = profileTaskManager;
        this.accountService = accountService;

        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

        _avatar.setValue(user.photo());
    }

    String getInitialName() {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
        return user.name();
    }

    String getInitialBio() {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
        return user.bio();
    }


    Uri getInitialAvatar() {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
        return user.photo();
    }

    void updateProfile(@NonNull String name, @NonNull String bio, @NonNull Context context) {

        Uri avatar = this.avatar.getValue();
        Uri currentAvatar = getInitialAvatar();

        UserUpdateRequest request = new UserUpdateRequest()
                .setName(name)
                .setBio(bio);

        if (!Objects.equals(avatar, currentAvatar))
            request.setAvatar(avatar);

       if (!accountService.validateUserUpdate(request)) {
            _error.setValue(new Error(R.string.error_invalid_profile_update_request));
            return;
        }

        LiveData<WorkInfo> workInfoLiveData = profileTaskManager.startProfileUpdateTask(request);

        _error.setValue(null);

        Observer<WorkInfo> observer = new Observer<>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                WorkInfo.State state = workInfo.getState();

                if(state.isFinished()) {
                    workInfoLiveData.removeObserver(this);
                    if (state == WorkInfo.State.FAILED)
                        Toast.makeText(context, R.string.error_profile_update, Toast.LENGTH_SHORT).show();
                }
            }
        };

        workInfoLiveData.observeForever(observer);
    }


    void setAvatar(@Nullable Uri uri) {
        _avatar.setValue(uri);
    }

}

record Error(@StringRes int messageId) {
}
package com.example.edunet.ui.screen.profile.update;

import android.content.ContentResolver;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.AvatarService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;

public class ProfileUpdateViewModel extends ViewModel {

    private final static AccountService accountService = AccountService.IMPL;
    private final static MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    private final MutableLiveData<Uri> _userPhoto = new MutableLiveData<>();
    final LiveData<Uri> userPhoto = _userPhoto;
    private final MutableLiveData<Message> _result = new MutableLiveData<>();
    final LiveData<Message> result = _result;
    private final static AvatarService avatarService = AvatarService.IMPL;


    {
        _userPhoto.setValue(accountService.getCurrentUser().photo());
    }

    String getInitialName() {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.ErrorMessages.CURRENT_USER_IS_NULL;
        return user.name();
    }

    void updateProfile(String name, ContentResolver contentResolver) {
        Uri photo = userPhoto.getValue();

        UserChangeRequest request = new UserChangeRequest();
        request.setName(name);

        avatarService.saveAvatarOfCurrentUser(
                photo,
                photo == null ? null : '.' + mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(photo)),
                uri -> _updateProfile(request.setPhoto(uri)),
                v -> _updateProfile(request),
                e -> _result.setValue(new Message(true, e.getMessage()))
        );

    }

    private void _updateProfile(UserChangeRequest request) {
        accountService.updateCurrentUser(request,
                e -> {
                    boolean haveException = e != null;
                    _result.setValue(new Message(haveException, !haveException ? null : e.getMessage()));
                }
        );
    }

    void setTemporaryImage(Uri uri) {
        _userPhoto.setValue(uri);
    }

}

record Message(boolean haveError, @Nullable String message) {
}
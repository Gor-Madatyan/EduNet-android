package com.example.edunet.data.service.impl.account;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.example.edunet.R;
import com.example.edunet.data.service.api.AccountService;
import com.example.edunet.data.service.api.storage.AvatarService;
import com.example.edunet.data.service.exception.ServiceException;

import javax.inject.Inject;

public final class ProfileManager {
    private final AccountService accountService;
    private final AvatarService avatarService;

    @Inject
    ProfileManager(AccountService accountService, AvatarService avatarService) {
        this.accountService = accountService;
        this.avatarService = avatarService;
    }

    /**
     * User update request that receives optional name and local avatar url to update user profile.
     */
    public static class UserUpdateRequest {
        private final com.example.edunet.data.service.model.UserUpdateRequest baseRequest = new com.example.edunet.data.service.model.UserUpdateRequest();

        public UserUpdateRequest setName(String name) {
            baseRequest.setName(name);
            return this;
        }

        public String getName() {
            return baseRequest.getName();
        }

        public boolean isNameSet() {
            return baseRequest.isNameSet();
        }

        public boolean isBioSet() {
            return baseRequest.isBioSet();
        }

        public UserUpdateRequest setBio(String bio) {
            baseRequest.setBio(bio);
            return this;
        }

        public String getBio() {
            return baseRequest.getBio();
        }

        public UserUpdateRequest setAvatar(Uri avatar) {
            baseRequest.setAvatar(avatar);
            return this;
        }

        public Uri getAvatar() {
            return baseRequest.getAvatar();
        }

        public boolean isAvatarSet() {
            return baseRequest.isAvatarSet();
        }
    }


    /**
     * Updates the user profile while taking the responsibility of storing avatar.
     * This will be called directly only in persistent operations.
     *
     * @param request  user update request
     * @param onResult callback called on result
     * @see ProfileTaskManager#startProfileUpdateTask(UserUpdateRequest)
     */
    public void updateProfile(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        if (!validateUserUpdate(request)) {
            onResult.accept(new ServiceException(R.string.error_invalid_profile_update_request));
            return;
        }
        com.example.edunet.data.service.model.UserUpdateRequest endRequest = new com.example.edunet.data.service.model.UserUpdateRequest();

        if (request.isNameSet()) endRequest.setName(request.getName());
        if (request.isBioSet()) endRequest.setBio(request.getBio());
        if (request.isAvatarSet() && request.getAvatar() == null) endRequest.setAvatar(null);

        if (!request.isAvatarSet() || request.getAvatar() == null) {
            accountService.updateCurrentUser(endRequest, onResult);
            return;
        }

        avatarService.saveAvatar(
                request.getAvatar(),
                newAvatar -> accountService.updateCurrentUser(endRequest.setAvatar(newAvatar), onResult),
                onResult
        );
    }

    public boolean validateUserUpdate(@NonNull UserUpdateRequest request) {
        com.example.edunet.data.service.model.UserUpdateRequest baseRequest = new com.example.edunet.data.service.model.UserUpdateRequest();
        if (request.isNameSet()) baseRequest.setName(request.getName());
        if (request.isBioSet()) baseRequest.setBio(request.getBio());

        return (accountService.validateUserUpdate(baseRequest) &&
                (request.getAvatar() == null || avatarService.validateAvatar(request.getAvatar())));
    }
}
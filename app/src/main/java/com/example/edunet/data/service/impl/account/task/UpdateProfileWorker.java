package com.example.edunet.data.service.impl.account.task;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.impl.account.ProfileManager;
import com.google.common.util.concurrent.ListenableFuture;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class UpdateProfileWorker extends ListenableWorker {

    public final static String NAME_KEY = "NAME";
    public final static String AVATAR_KEY = "AVATAR";
    public final static String IS_NAME_SET_KEY = "IS_NAME_SET";
    public final static String IS_AVATAR_SET_KEY = "IS_AVATAR_SET";

    private final ProfileManager profileManager;

    @AssistedInject
    UpdateProfileWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            ProfileManager profileManager) {
        super(context, workerParams);
        this.profileManager = profileManager;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ProfileManager.UserUpdateRequest request = getUserUpdateRequest();

        return CallbackToFutureAdapter.getFuture(completer -> {
            Consumer<ServiceException> callback = e -> {
                if (e != null) completer.setException(e);
                else completer.set(Result.success());
            };

            profileManager.updateProfile(request, callback);
            return callback;
        });
    }

    private ProfileManager.UserUpdateRequest getUserUpdateRequest() {
        Data data = getInputData();
        ProfileManager.UserUpdateRequest request = new ProfileManager.UserUpdateRequest();
        String name = data.getString(NAME_KEY);
        String avatar = data.getString(AVATAR_KEY);

        if (data.getBoolean(IS_NAME_SET_KEY, false))
            request.setName(name);

        if (data.getBoolean(IS_AVATAR_SET_KEY, false))
            request.setAvatar(avatar == null ? null : Uri.parse(avatar));

        return request;
    }

    public static Data getDataFromUserUpdateRequest(ProfileManager.UserUpdateRequest request) {
        Uri avatar = request.getAvatar();
        String name = request.getName();

        return new Data.Builder()
                .putBoolean(IS_NAME_SET_KEY, request.isNameSet())
                .putBoolean(IS_AVATAR_SET_KEY, request.isAvatarSet())

                .putString(NAME_KEY, name)
                .putString(AVATAR_KEY, avatar == null ? null : avatar.toString()).build();
    }

}

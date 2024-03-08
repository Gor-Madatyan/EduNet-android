package com.example.edunet.data.service.task.account.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.google.common.util.concurrent.ListenableFuture;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class UpdateProfileWorker extends ListenableWorker {

    public final static String NAME_KEY = "NAME";
    public final static String BIO_KEY = "BIO";
    public final static String AVATAR_KEY = "AVATAR";
    public final static String IS_NAME_SET_KEY = "IS_NAME_SET";
    public final static String IS_BIO_SET_KEY = "IS_BIO_SET";
    public final static String IS_AVATAR_SET_KEY = "IS_AVATAR_SET";

    private final AccountService accountService;

    @AssistedInject
    UpdateProfileWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            AccountService accountService) {
        super(context, workerParams);
        this.accountService = accountService;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        UserUpdateRequest request = getUserUpdateRequest();

        return CallbackToFutureAdapter.getFuture(completer -> {
            Consumer<ServiceException> callback = e -> {
                if (e != null) completer.setException(e);
                else completer.set(Result.success());
            };

            accountService.updateCurrentUser(request, callback);
            return callback;
        });
    }

    private UserUpdateRequest getUserUpdateRequest() {
        Data data = getInputData();
        UserUpdateRequest request = new UserUpdateRequest();
        String name = data.getString(NAME_KEY);
        String bio = data.getString(BIO_KEY);
        String avatar = data.getString(AVATAR_KEY);

        if (data.getBoolean(IS_NAME_SET_KEY, false))
            request.setName(name);

        if (data.getBoolean(IS_AVATAR_SET_KEY, false))
            request.setAvatar(avatar == null ? null : Uri.parse(avatar));

        if (data.getBoolean(IS_BIO_SET_KEY, false))
            request.setBio(bio);

        return request;
    }

    public static Data getDataFromUserUpdateRequest(UserUpdateRequest request) {
        Uri avatar = request.getAvatar();
        String name = request.getName();
        String bio = request.getBio();

        return new Data.Builder()
                .putBoolean(IS_NAME_SET_KEY, request.isNameSet())
                .putBoolean(IS_AVATAR_SET_KEY, request.isAvatarSet())
                .putBoolean(IS_BIO_SET_KEY, request.isBioSet())

                .putString(NAME_KEY, name)
                .putString(BIO_KEY, bio)
                .putString(AVATAR_KEY, avatar == null ? null : avatar.toString()).build();
    }

}

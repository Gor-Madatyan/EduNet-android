package com.example.edunet.data.service.task.community.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class CommunityUpdateWorker extends ListenableWorker {
    private static final String ID_KEY = "ID";
    private static final String AVATAR_KEY = "AVATAR";
    private static final String NAME_KEY = "NAME";
    private static final String DESCRIPTION_KEY = "DESCRIPTION";

    private static final String IS_AVATAR_SET_KEY = "IS_AVATAR_SET";
    private static final String IS_NAME_SET_KEY = "IS_NAME_SET";
    private static final String IS_DESCRIPTION_SET_KEY = "IS_DESCRIPTION_SET";

    private final CommunityService communityService;

    @AssistedInject
    CommunityUpdateWorker(@Assisted @NonNull Context appContext,
                          @Assisted @NonNull WorkerParameters workerParams,
                          CommunityService communityService) {
        super(appContext, workerParams);
        this.communityService = communityService;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        CommunityUpdateRequest request = getCommunityUpdateRequestFromData();

        return CallbackToFutureAdapter.getFuture(completer -> {
            Consumer<ServiceException> callBack = e -> {
                if (e == null) completer.set(Result.success());
                else completer.setException(e);
            };

            communityService.updateCommunity(request, callBack);
            return callBack;
        });
    }

    public static Data getDataFromCommunityUpdateRequest(CommunityUpdateRequest request) {
        return new Data.Builder()
                .putBoolean(IS_AVATAR_SET_KEY, request.isAvatarSet())
                .putBoolean(IS_NAME_SET_KEY, request.isNameSet())
                .putBoolean(IS_DESCRIPTION_SET_KEY, request.isDescriptionSet())

                .putString(ID_KEY, request.getId())
                .putString(AVATAR_KEY, request.getAvatar() == null ? null : request.getAvatar().toString())
                .putString(NAME_KEY, request.getName())
                .putString(DESCRIPTION_KEY, request.getDescription()).build();
    }

    private CommunityUpdateRequest getCommunityUpdateRequestFromData() {
        Data data = getInputData();
        CommunityUpdateRequest request = new CommunityUpdateRequest(Objects.requireNonNull(data.getString(ID_KEY)));

        String name = data.getString(NAME_KEY);
        String description = data.getString(DESCRIPTION_KEY);
        String avatar = data.getString(AVATAR_KEY);

        if (data.getBoolean(IS_NAME_SET_KEY, false))
            request.setName(name);

        if (data.getBoolean(IS_AVATAR_SET_KEY, false))
            request.setAvatar(avatar == null ? null : Uri.parse(avatar));

        if (data.getBoolean(IS_DESCRIPTION_SET_KEY, false))
            request.setDescription(description);

        return request;
    }


}

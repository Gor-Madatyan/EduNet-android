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
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.google.common.util.concurrent.ListenableFuture;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class CommunityCreateWorker extends ListenableWorker {
    private static final String AVATAR_KEY = "AVATAR";
    private static final String NAME_KEY = "NAME";
    private static final String DESCRIPTION_KEY = "DESCRIPTION";
    private static final String ANCESTOR_KEY = "ANCESTOR";

    private final CommunityService communityService;

    @AssistedInject
    CommunityCreateWorker(@Assisted @NonNull Context appContext,
                          @Assisted @NonNull WorkerParameters workerParams,
                          CommunityService communityService) {
        super(appContext, workerParams);
        this.communityService = communityService;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        CommunityCreateRequest request = getCommunityCreateRequestFromData();

        return CallbackToFutureAdapter.getFuture(completer -> {
            Consumer<ServiceException> callBack = e -> {
                if (e == null) completer.set(Result.success());
                else completer.setException(e);
            };

            communityService.createCommunity(request, callBack);
            return callBack;
        });
    }

    public static Data getDataFromCommunityCreateRequest(CommunityCreateRequest request) {
        return new Data.Builder()
                .putString(AVATAR_KEY, request.getAvatar() == null ? null : request.getAvatar().toString())
                .putString(NAME_KEY, request.getName())
                .putString(DESCRIPTION_KEY, request.getDescription())
                .putString(ANCESTOR_KEY,request.getAncestor()).build();
    }

    private CommunityCreateRequest getCommunityCreateRequestFromData() {
        Data data = getInputData();
        String name = data.getString(NAME_KEY);
        String description = data.getString(DESCRIPTION_KEY);
        String avatar = data.getString(AVATAR_KEY);
        String ancestor = data.getString(ANCESTOR_KEY);

        return new CommunityCreateRequest().setName(name)
                .setDescription(description)
                .setAvatar(avatar == null ? null : Uri.parse(avatar))
                .setAncestor(ancestor);
    }


}

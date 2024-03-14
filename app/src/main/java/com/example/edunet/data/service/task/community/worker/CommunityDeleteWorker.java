package com.example.edunet.data.service.task.community.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.exception.ServiceException;
import com.google.common.util.concurrent.ListenableFuture;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class CommunityDeleteWorker extends ListenableWorker {
    private static final String ID_KEY = "ID";
    private final CommunityService communityService;

    @AssistedInject
    CommunityDeleteWorker(
            @Assisted @NonNull Context appContext,
            @Assisted @NonNull WorkerParameters workerParams,
            CommunityService communityService) {
        super(appContext, workerParams);
        this.communityService = communityService;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Consumer<ServiceException> callBack = e -> {
                if (e == null) completer.set(Result.success());
                else completer.setException(e);
            };

            communityService.deleteCommunity(getCommunityId(),callBack);
            return callBack;
        });
    }

    public static Data getDataFromId(@NonNull String id) {
        return new Data.Builder()
                .putString(ID_KEY, id)
                .build();
    }

    private String getCommunityId() {
        return getInputData().getString(ID_KEY);
    }

}

package com.example.edunet.data.service.task.community;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.task.community.worker.CommunityCreateWorker;
import com.example.edunet.data.service.task.community.worker.CommunityUpdateWorker;

import javax.inject.Inject;

public class CommunityTaskManager {
    private final WorkManager workManager;

    @Inject
    CommunityTaskManager(WorkManager workManager){
        this.workManager = workManager;
    }

    public LiveData<WorkInfo> startCommunityCreateTask(@NonNull CommunityCreateRequest request) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CommunityCreateWorker.class)
                .setInputData(CommunityCreateWorker.getDataFromCommunityCreateRequest(request))
                .build();

        workManager.enqueue(workRequest);

        return workManager.getWorkInfoByIdLiveData(workRequest.getId());

    }

    public LiveData<WorkInfo> startCommunityUpdateTask(@NonNull CommunityUpdateRequest request){
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CommunityUpdateWorker.class)
                .setInputData(CommunityUpdateWorker.getDataFromCommunityUpdateRequest(request))
                .build();

        workManager.enqueueUniqueWork(
                request.getId(),
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest);

        return workManager.getWorkInfoByIdLiveData(workRequest.getId());
    }
}

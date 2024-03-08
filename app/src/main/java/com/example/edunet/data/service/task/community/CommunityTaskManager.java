package com.example.edunet.data.service.task.community;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.edunet.data.service.model.CommunityModifyRequest;
import com.example.edunet.data.service.task.community.worker.CommunityCreateWorker;

import javax.inject.Inject;

public class CommunityTaskManager {
    private final WorkManager workManager;

    @Inject
    CommunityTaskManager(WorkManager workManager){
        this.workManager = workManager;
    }

    public LiveData<WorkInfo> startCommunityCreateTask(@NonNull CommunityModifyRequest request) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CommunityCreateWorker.class)
                .setInputData(CommunityCreateWorker.getDataFromCommunityCreateRequest(request))
                .build();

        workManager.enqueue(workRequest);

        return workManager.getWorkInfoByIdLiveData(workRequest.getId());

    }
}

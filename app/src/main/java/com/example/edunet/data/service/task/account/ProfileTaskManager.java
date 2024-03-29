package com.example.edunet.data.service.task.account;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.task.account.worker.UpdateProfileWorker;

import javax.inject.Inject;


public final class ProfileTaskManager {
    public static String uniqueProfileUpdateTaskName = "updateProfile";
    private final WorkManager workManager;

    @Inject
    ProfileTaskManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    public LiveData<WorkInfo> startProfileUpdateTask(@NonNull UserUpdateRequest request) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateProfileWorker.class)
                .setInputData(UpdateProfileWorker.getDataFromUserUpdateRequest(request))
                .build();

        workManager.enqueueUniqueWork(
                uniqueProfileUpdateTaskName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
        );

        return workManager.getWorkInfoByIdLiveData(workRequest.getId());

    }

}

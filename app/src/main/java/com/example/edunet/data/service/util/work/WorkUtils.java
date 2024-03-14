package com.example.edunet.data.service.util.work;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;

public final class WorkUtils {
    private WorkUtils(){}

    public static void observe(@NonNull Context context, LiveData<WorkInfo> workInfoLiveData, @StringRes int message){

        Observer<WorkInfo> observer = new Observer<>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                WorkInfo.State state = workInfo.getState();

                if (state.isFinished()) {
                    workInfoLiveData.removeObserver(this);
                    if (state == WorkInfo.State.FAILED)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            }
        };

        workInfoLiveData.observeForever(observer);
    }
}

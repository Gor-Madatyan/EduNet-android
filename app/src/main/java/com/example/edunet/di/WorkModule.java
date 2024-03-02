package com.example.edunet.di;

import android.content.Context;

import androidx.work.WorkManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class WorkModule {
    @Provides
    public WorkManager provideWorkManager(@ApplicationContext Context context){
        return WorkManager.getInstance(context);
    }
}

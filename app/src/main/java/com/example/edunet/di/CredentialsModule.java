package com.example.edunet.di;

import android.content.Context;

import androidx.credentials.CredentialManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class CredentialsModule {
    @Provides
    public static CredentialManager provideCredentialManager(@ApplicationContext Context context) {
        return CredentialManager.create(context);
    }
}

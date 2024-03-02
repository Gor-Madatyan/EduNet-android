package com.example.edunet.di;

import com.example.edunet.data.service.api.AccountService;
import com.example.edunet.data.service.api.StorageService;
import com.example.edunet.data.service.api.storage.AvatarService;
import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.impl.StorageServiceImpl;
import com.example.edunet.data.service.impl.storage.AvatarServiceImpl;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class ServiceModule {
    @Binds
    public abstract AccountService bindAccountService(AccountServiceImpl impl);

    @Binds
    public abstract StorageService bindStorageService(StorageServiceImpl impl);

    @Binds
    public abstract AvatarService bindAvatarService(AvatarServiceImpl impl);
}

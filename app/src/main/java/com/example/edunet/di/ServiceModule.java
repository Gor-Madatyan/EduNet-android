package com.example.edunet.di;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.impl.CommunityServiceImpl;

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
    public abstract CommunityService bindCommunityService(CommunityServiceImpl impl);
}

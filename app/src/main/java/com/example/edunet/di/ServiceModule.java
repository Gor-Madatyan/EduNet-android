package com.example.edunet.di;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.MessagingService;
import com.example.edunet.data.service.NotificationsService;
import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.impl.CommunityServiceImpl;
import com.example.edunet.data.service.impl.MessagingServiceImpl;
import com.example.edunet.data.service.impl.NotificationsServiceImpl;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@SuppressWarnings("unused")
@InstallIn(SingletonComponent.class)
@Module
public abstract class ServiceModule {
    @Binds
    public abstract AccountService bindAccountService(AccountServiceImpl impl);
    @Binds
    public abstract CommunityService bindCommunityService(CommunityServiceImpl impl);
    @Binds
    public abstract MessagingService bindMessagingService(MessagingServiceImpl impl);
    @Binds
    public abstract NotificationsService bindNotificationsService(NotificationsServiceImpl impl);
}

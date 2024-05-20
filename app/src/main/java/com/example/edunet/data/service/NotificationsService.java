package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.data.service.util.paginator.Paginator;

public interface NotificationsService {
    @NonNull
    Paginator<UserNotification> getDescendingNotificationPaginator(@NonNull String source, int limit);

    void manageCommunitySubscription(@NonNull String communityId, boolean subscribe, @NonNull Consumer<ServiceException> onResult);
}

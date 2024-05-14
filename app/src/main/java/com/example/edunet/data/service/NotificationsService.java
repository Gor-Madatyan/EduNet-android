package com.example.edunet.data.service;

import androidx.annotation.NonNull;

import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.data.service.util.paginator.Paginator;

public interface NotificationsService {
    @NonNull
    Paginator<UserNotification> getDescendingNotificationPaginator(String source, int limit);
}

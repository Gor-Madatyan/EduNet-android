package com.example.edunet.ui.screen.notifications;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.edunet.R;
import com.example.edunet.data.service.NotificationsService;
import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.ui.util.adapter.impl.LazyAdapter;
import com.example.edunet.ui.util.adapter.impl.NotificationsAdapter;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationsViewModel extends ViewModel {
    private static final int PAGINATOR_LiMIT = 20;
    private final NotificationsService notificationsService;
    private String communityId;
    private LazyAdapter<?, UserNotification> notificationsAdapter;

    @Inject
    NotificationsViewModel(NotificationsService notificationsService){
        this.notificationsService = notificationsService;

    }

    LazyAdapter<?, UserNotification> getNotificationsAdapter(@NonNull String communityId){
        if(communityId.equals(this.communityId))
            return notificationsAdapter;

        this.communityId = communityId;
        notificationsAdapter = new LazyAdapter<>(
                new NotificationsAdapter(new ArrayList<>(), R.layout.user_notification_element),
                notificationsService.getDescendingNotificationPaginator(communityId, PAGINATOR_LiMIT)
        );
        return notificationsAdapter;

    }

}
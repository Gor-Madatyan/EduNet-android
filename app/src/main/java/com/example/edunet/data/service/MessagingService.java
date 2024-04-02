package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.util.common.Paginator;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public interface MessagingService {
    void sendMessage(@NonNull String message,
                     @NonNull String receiverId,
                     @NonNull Consumer<ServiceException> onResult);

    Paginator<Message> getDescendingMessagePaginator(String sourceId, int limit);

    void listenNewMessages(@NonNull LifecycleOwner lifecycleOwner,
                           @NonNull String sourceId,
                           @NonNull Date after,
                           @NonNull Consumer<List<Message>> onSuccess,
                           @NonNull Consumer<ServiceException> onFailure);

    boolean isCurrentUserOwner(Message message);
}

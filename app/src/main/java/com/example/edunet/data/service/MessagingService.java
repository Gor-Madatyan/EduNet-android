package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.util.paginator.Paginator;

import java.io.EOFException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public interface MessagingService {
    void sendMessage(@NonNull String message,
                     @NonNull String receiverId,
                     @NonNull Consumer<ServiceException> onResult);

    void getLastMessage(String sourceId, Consumer<Message> onSuccess, Consumer<Exception> onFailure);

    Paginator<Message> getDescendingMessagePaginator(String sourceId, int limit);

    void listenNewMessages(@NonNull LifecycleOwner lifecycleOwner,
                           @NonNull String sourceId,
                           @Nullable Date after,
                           @NonNull Consumer<List<Message>> onSuccess,
                           @NonNull Consumer<ServiceException> onFailure);

    default void listenNewMessages(@NonNull LifecycleOwner lifecycleOwner,
                                   @NonNull String sourceId,
                                   @NonNull Consumer<List<Message>> onSuccess,
                                   @NonNull Consumer<ServiceException> onFailure) {
        getLastMessage(sourceId,
                message ->
                        listenNewMessages(lifecycleOwner, sourceId, message.date(), onSuccess, onFailure),
                e -> {
                    if (e instanceof EOFException)
                        listenNewMessages(lifecycleOwner, sourceId, null, onSuccess, onFailure);
                }
        );
    }


    boolean isCurrentUserOwner(Message message);
}

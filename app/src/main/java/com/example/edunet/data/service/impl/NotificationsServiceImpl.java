package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userNotificationFromFirestoreUserNotification;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import com.example.edunet.R;
import com.example.edunet.data.service.NotificationsService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.data.service.model.UserOperation;
import com.example.edunet.data.service.util.firebase.paginator.QueryPaginator;
import com.example.edunet.data.service.util.paginator.Paginator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class NotificationsServiceImpl implements NotificationsService {

    private final FirebaseFirestore firestore;

    @SuppressWarnings("unused")
    public static class FirestoreUserNotification{
        private Role membersType;
        private UserOperation operationType;
        private boolean arePending;
        private Date timestamp;
        private List<String> users;

        public Role getMembersType() {
            return membersType;
        }

        public UserOperation getOperationType() {
            return operationType;
        }

        public boolean getArePending() {
            return arePending;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public List<String> getUsers() {
            return users;
        }
    }

    @Inject
    NotificationsServiceImpl(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @NonNull
    @Override
    public Paginator<UserNotification> getDescendingNotificationPaginator(String source, int limit) {
        CollectionReference notificationsReference = getNotificationsCollection(source);
        Paginator<Pair<String, FirestoreUserNotification>> in = new QueryPaginator<>(
                notificationsReference
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                , limit
                , FirestoreUserNotification.class);

        return new Paginator<>() {
            @Override
            public void next(Consumer<List<UserNotification>> onSuccess, androidx.core.util.Consumer<Exception> onFailure) {
                in.next(
                        list -> onSuccess.accept(list.stream().map(pair -> userNotificationFromFirestoreUserNotification(pair.second)).collect(Collectors.toList())),
                        e -> onFailure.accept(new ServiceException(R.string.error_cant_load_notification, e))
                );
            }

            @Override
            public boolean isLoading() {
                return in.isLoading();
            }

            @Override
            public boolean hasFailure() {
                return in.hasFailure();
            }

            @Override
            public boolean isEofReached() {
                return in.isEofReached();
            }
        };    }


    private CollectionReference getNotificationsCollection(@NonNull String source) {
        DocumentReference sourceReference = firestore.collection("communities").document(source);
        return sourceReference.collection("notifications");
    }
}

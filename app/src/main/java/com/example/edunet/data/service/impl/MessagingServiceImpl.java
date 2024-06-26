package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.messageFromFirestoreMessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.R;
import com.example.edunet.data.service.MessagingService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.paginator.QueryPaginator;
import com.example.edunet.data.service.util.paginator.Paginator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class MessagingServiceImpl implements MessagingService {
    private final FirebaseFirestore firestore;
    private final AccountServiceImpl accountService;

    @SuppressWarnings("unused")
    public static class FirestoreMessage {
        private String message;
        private String senderId;
        @ServerTimestamp
        private Timestamp timestamp;

        public FirestoreMessage() {
        }

        public FirestoreMessage(String message, String senderId) {
            this.message = message;
            this.senderId = senderId;
        }

        public String getMessage() {
            return message;
        }

        public String getSenderId() {
            return senderId;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }

    @Inject
    MessagingServiceImpl(FirebaseFirestore firestore, AccountServiceImpl accountService) {
        this.firestore = firestore;
        this.accountService = accountService;
    }

    @Override
    public void sendMessage(@NonNull String message, @NonNull String receiverId, @NonNull Consumer<ServiceException> onResult) {
        assert !message.isEmpty();
        CollectionReference messagesReference = getMessagesCollection(receiverId);
        String uid = accountService.getUid();
        assert uid != null;

        FirestoreMessage newMessage = new FirestoreMessage(message, uid);
        messagesReference.add(newMessage)
                .addOnCompleteListener(result -> {
                    Exception e = result.getException();
                    onResult.accept(e == null ? null : new ServiceException(R.string.error_cant_send_message, e));
                });
    }

    @Override
    public void getLastMessage(String sourceId, Consumer<Message> onSuccess, Consumer<Exception> onFailure) {
        CollectionReference messagesReference = getMessagesCollection(sourceId);
        messagesReference.orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        onFailure.accept(new EOFException());
                        return;
                    }
                    onSuccess.accept(
                            messageFromFirestoreMessage(
                                    Objects.requireNonNull(snapshots.getDocuments()
                                            .get(0)
                                            .toObject(FirestoreMessage.class)
                                    )
                            )
                    );

                })
                .addOnFailureListener(e ->
                        onFailure.accept(new ServiceException(R.string.error_cant_load_message, e)));
    }

    @Override
    public Paginator<Message> getDescendingMessagePaginator(String sourceId, int limit) {
        CollectionReference messagesReference = getMessagesCollection(sourceId);
        Paginator<Pair<String, FirestoreMessage>> in = new QueryPaginator<>(
                messagesReference
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                , limit
                , FirestoreMessage.class);

        return new Paginator<>() {
            @Override
            public void next(androidx.core.util.Consumer<List<Message>> onSuccess, androidx.core.util.Consumer<Exception> onFailure) {
                in.next(
                        list -> onSuccess.accept(list.stream().map(pair -> messageFromFirestoreMessage(pair.second)).collect(Collectors.toList())),
                        e -> onFailure.accept(new ServiceException(R.string.error_cant_load_message, e))
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
        };
    }

    @Override
    public void listenNewMessages(@NonNull LifecycleOwner lifecycleOwner, @NonNull String sourceId, @Nullable Date after, @NonNull Consumer<List<Message>> onSuccess, @NonNull Consumer<ServiceException> onFailure) {
        CollectionReference messages = getMessagesCollection(sourceId);
        Query query = messages.orderBy("timestamp", Query.Direction.DESCENDING);
        if (after != null) query = query.endBefore(after);

        FirestoreUtils.attachObserver(query.addSnapshotListener(
                (snapshots, e) -> {
                    if (e != null) {
                        onFailure.accept(new ServiceException(R.string.error_cant_load_message, e));
                        return;
                    }
                    assert snapshots != null;
                    List<Message> newMessages = new ArrayList<>();


                    for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            QueryDocumentSnapshot snapshot = documentChange.getDocument();
                            FirestoreMessage firestoreMessage = snapshot.toObject(FirestoreMessage.class);
                            if (firestoreMessage.timestamp == null)
                                firestoreMessage.timestamp = Timestamp.now();
                            newMessages.add(messageFromFirestoreMessage(firestoreMessage));
                        }
                    }

                    onSuccess.accept(newMessages);
                }
        ), lifecycleOwner);
    }

    @Override
    public boolean isCurrentUserOwner(Message message) {
        String uid = accountService.getUid();
        assert uid != null;

        return Objects.equals(message.senderId(), uid);
    }

    private CollectionReference getMessagesCollection(@NonNull String source) {
        DocumentReference sourceReference = firestore.collection("communities").document(source);
        return sourceReference.collection("messages");
    }
}

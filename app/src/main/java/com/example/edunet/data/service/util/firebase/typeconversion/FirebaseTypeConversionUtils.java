package com.example.edunet.data.service.util.firebase.typeconversion;

import androidx.annotation.Nullable;

import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.impl.CommunityServiceImpl;
import com.example.edunet.data.service.impl.MessagingServiceImpl;
import com.example.edunet.data.service.impl.NotificationsServiceImpl;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.util.UriUtils;
import com.google.firebase.auth.FirebaseUser;

public final class FirebaseTypeConversionUtils {

    private FirebaseTypeConversionUtils() {
    }

    @Nullable
    public static User userFromAuthUser(@Nullable FirebaseUser user) {
        if (user == null) return null;

        return new User(user.getUid(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhotoUrl(),
                null);
    }

    public static User userFromFireStoreUser(@Nullable String uid, @Nullable AccountServiceImpl.FirestoreUser user) {
        if (user == null) return null;
        assert uid != null;

        return new User(uid,
                user.getName(),
                user.getEmail(),
                UriUtils.safeParse(user.getAvatar()),
                user.getBio()
        );
    }

    @Nullable
    public static Community communityFromFirestoreCommunity(@Nullable String id, @Nullable CommunityServiceImpl.FirestoreCommunity firestoreCommunity) {
        if (firestoreCommunity == null) return null;
        assert id != null;

        return new Community(
                firestoreCommunity.getName(),
                firestoreCommunity.getDescription(),
                UriUtils.safeParse(firestoreCommunity.getAvatar()),
                firestoreCommunity.getAdmins(),
                firestoreCommunity.getAdminsQueue(),
                firestoreCommunity.getParticipants(),
                firestoreCommunity.getParticipantsQueue(),
                firestoreCommunity.getGraduated(),
                firestoreCommunity.getGraduations(),
                firestoreCommunity.getAncestor(),
                id,
                firestoreCommunity.getOwnerId()
        );
    }

    public static Message messageFromFirestoreMessage(MessagingServiceImpl.FirestoreMessage message) {
        return new Message(message.getMessage(), message.getSenderId(), message.getTimestamp().toDate());
    }

    public static UserNotification userNotificationFromFirestoreUserNotification(NotificationsServiceImpl.FirestoreUserNotification userNotification) {
        return new UserNotification(
                userNotification.getMembersType(),
                userNotification.getOperationType(),
                userNotification.getArePending(),
                userNotification.getTimestamp(),
                userNotification.getUsers().toArray(new String[0])
        );
    }
}

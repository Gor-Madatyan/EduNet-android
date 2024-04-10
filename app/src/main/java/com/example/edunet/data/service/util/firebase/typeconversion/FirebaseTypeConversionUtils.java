package com.example.edunet.data.service.util.firebase.typeconversion;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.example.edunet.common.util.UriUtils;
import com.example.edunet.data.service.impl.AccountServiceImpl;
import com.example.edunet.data.service.impl.CommunityServiceImpl;
import com.example.edunet.data.service.impl.MessagingServiceImpl;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.model.User;
import com.google.firebase.auth.FirebaseUser;

public final class FirebaseTypeConversionUtils {

    private FirebaseTypeConversionUtils() {
    }

    @Nullable
    public static User userFromAuthUser(@Nullable FirebaseUser user) {
        if (user == null) return null;

        return new User(user.getUid(),
                user.getDisplayName(),
                user.getPhotoUrl(),
                null);
    }

    public static User userFromFireStoreUser(@Nullable String uid, @Nullable AccountServiceImpl.FirestoreUser user) {
        if (user == null) return null;
        assert uid != null;

        return new User(uid,
                user.getName(),
                user.getAvatar() == null ? null : Uri.parse(user.getAvatar()),
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
                firestoreCommunity.getAncestor(),
                id,
                firestoreCommunity.getOwnerId()
        );
    }

    public static Message messageFromFirestoreMessage(MessagingServiceImpl.FirestoreMessage message) {
        return new Message(message.getMessage(), message.getSenderId(), message.getTimestamp().toDate());
    }
}

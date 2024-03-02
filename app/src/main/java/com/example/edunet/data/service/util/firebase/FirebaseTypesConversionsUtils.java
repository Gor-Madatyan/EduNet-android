package com.example.edunet.data.service.util.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public final class FirebaseTypesConversionsUtils {

    private FirebaseTypesConversionsUtils(){}

    @NonNull
    public static UserProfileChangeRequest FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest(@NonNull UserUpdateRequest abstractRequest) {
        UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();

        String name = abstractRequest.getName();
        Uri photoUri = abstractRequest.getAvatar();


        if (abstractRequest.isNameSet())
            request.setDisplayName(name);

        if (abstractRequest.isAvatarSet())
            request.setPhotoUri(photoUri);

        return request.build();
    }

    @Nullable
    public static User userFromFireBaseUser(FirebaseUser user) {
        if (user == null) return null;
        return new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl(), user.isAnonymous());
    }
}

package com.example.edunet.data.service.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseTypesConversionsUtils {

    @NonNull
    public static UserProfileChangeRequest fireBaseUserChangeRequestFromAbstractUserChangeRequest(@NonNull UserChangeRequest abstractRequest) {
        var request = new UserProfileChangeRequest.Builder();

        String name = abstractRequest.getNewName();
        String photoUri = abstractRequest.getNewPhotoUri();


        if (abstractRequest.isNewNameSet())
            request.setDisplayName(name);

        if (abstractRequest.isNewPhotoUriSet())
            request.setPhotoUri(photoUri != null ? Uri.parse(photoUri) : null);

        return  request.build();
    }

    public static User userFromFireBaseUser( FirebaseUser user) {
        if(user == null) return null;
        return new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl());
    }
}

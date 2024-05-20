package com.example.edunet.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class FirebaseModule {
    @Provides
    public static FirebaseAuth provideAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    public static FirebaseStorage provideStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    public static FirebaseFirestore provideFireStore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    public static FirebaseMessaging provideMessaging() {
        return FirebaseMessaging.getInstance();
    }
}

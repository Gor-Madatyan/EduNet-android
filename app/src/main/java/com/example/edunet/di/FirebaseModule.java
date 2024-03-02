package com.example.edunet.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class FirebaseModule {
    @Provides
    FirebaseAuth provideAuth(){
        return FirebaseAuth.getInstance();
    }

    @Provides
    FirebaseStorage provideStorage(){
        return FirebaseStorage.getInstance();
    }
}

package com.example.edunet.data.service.util.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.storage.FirebaseStorage;

public final class EmulatorSuiteUtils {
    private EmulatorSuiteUtils() {
    }

    public static void useAuthEmulator(){
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
    }

    public static void useFirestoreEmulator() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build();
        firestore.setFirestoreSettings(settings);
    }

    public static void useStorageEmulator() {
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
    }

    public static void useAllEmulators() {
        useAuthEmulator();
        useFirestoreEmulator();
        useStorageEmulator();
    }
}

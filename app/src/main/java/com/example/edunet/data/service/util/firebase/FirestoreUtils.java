package com.example.edunet.data.service.util.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class FirestoreUtils {
    private static final String TAG = FirestoreUtils.class.getSimpleName();

    private FirestoreUtils() {
    }

    public static void attachObserver(ListenerRegistration listener, LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onDestroy(owner);
                Log.i(TAG, "firebase snapshot listener removed");
                listener.remove();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> ListenerRegistration observeData(@NonNull Query query, @NonNull Class<T> clazz, @NonNull BiConsumer<Exception, Pair<String,T>[]> biConsumer) {
        return query.addSnapshotListener(
                (documents, e) -> {
                    if (e != null) {
                        biConsumer.accept(e, null);
                        return;
                    }
                    //because of the contract of OnEventListener
                    assert documents != null;

                    List<Pair<String,T>> objects = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : documents)
                        objects.add(new Pair<>(snapshot.getId(), snapshot.toObject(clazz)));

                    biConsumer.accept(null, objects.toArray(new Pair[0]));
                });
    }

}

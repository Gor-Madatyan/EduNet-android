package com.example.edunet.data.service.util.firebase;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class FirestoreUtils {
    private FirestoreUtils() {
    }

    public static ListenableFuture<Void> initializeDocument(DocumentReference reference, Object value) {
        return CallbackToFutureAdapter.getFuture(
                completer -> {
                    Consumer<Task<DocumentSnapshot>> callBack = task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) completer.set(null);
                            else reference.set(value)
                                    .addOnSuccessListener(v -> completer.set(null))
                                    .addOnFailureListener(completer::setException);

                        } else completer.setException(Objects.requireNonNull(task.getException()));
                    };

                    reference.get().addOnCompleteListener(
                            callBack::accept
                    );

                    return callBack;
                }
        );
    }

    public static <T> void loadData(
            @NonNull Class<T> clazz,
            @NonNull Collection<DocumentReference> documents,
            @NonNull Consumer<List<T>> onSuccess,
            @NonNull Consumer<Exception> onFailure) {

        List<T> data = new ArrayList<>();
        if (documents.isEmpty()) {
            onSuccess.accept(data);
            return;
        }
        for (DocumentReference document : documents) {
            document
                    .get()
                    .addOnSuccessListener(r -> {
                        T object = Objects.requireNonNull(r.toObject(clazz));
                        data.add(object);

                        if (data.size() == documents.size())
                            onSuccess.accept(data);
                    })
                    .addOnFailureListener(onFailure::accept);
        }
    }
}

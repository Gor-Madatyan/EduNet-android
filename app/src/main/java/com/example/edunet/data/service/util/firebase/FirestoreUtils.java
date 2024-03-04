package com.example.edunet.data.service.util.firebase;

import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Consumer;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public final class FirestoreUtils {
    private FirestoreUtils(){}

    public static ListenableFuture<Void> initializeDocument(DocumentReference reference, Object value){
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
}

package com.example.edunet.data.service.util.firebase.paginator;

import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import com.example.edunet.data.service.util.paginator.AbstractPaginator;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.EOFException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PortionedPaginator<T> extends AbstractPaginator<T> {
    private final Iterator<DocumentReference[]> referencePortions;

    public PortionedPaginator(Class<T> clazz, DocumentReference[][] references) {
        super(clazz, -1);
        referencePortions = Arrays.stream(references).iterator();
        if(references.length == 0) setEofReached();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void next(Consumer<List<Pair<String, T>>> onSuccess, Consumer<Exception> onFailure) {
        if (isEofReached()) {
            onFailure.accept(new EOFException());
            return;
        } else if (hasFailure()) {
            onFailure.accept(new IllegalStateException());
            return;
        }

        DocumentReference[] portion = referencePortions.next();
        Task<DocumentSnapshot>[] tasks = new Task[portion.length];

        if(!referencePortions.hasNext()){
            setEofReached();
        }

        for (int i = 0; i < portion.length; i++) {
            tasks[i] = portion[i].get();
        }

        setLoading(true);
        Tasks.<DocumentSnapshot>whenAllSuccess(tasks)
                .addOnCompleteListener(t->setLoading(false))
                .addOnSuccessListener(
                        list ->
                                onSuccess.accept(list.stream().map(
                                        snapshot -> new Pair<>(snapshot.getId(), Objects.requireNonNull(snapshot.toObject(clazz))
                                        )).collect(Collectors.toList()))
                ).addOnFailureListener(e->{
                    setFailed();
                    onFailure.accept(e);
                });
    }
}

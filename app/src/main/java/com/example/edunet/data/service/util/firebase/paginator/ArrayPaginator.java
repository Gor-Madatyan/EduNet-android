package com.example.edunet.data.service.util.firebase.paginator;

import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import com.example.edunet.data.service.util.common.AbstractPaginator;
import com.google.firebase.firestore.DocumentReference;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayPaginator<T> extends AbstractPaginator<T> {
    private final DocumentReference[] references;
    private int position = 0;
    private int successes = 0;

    public ArrayPaginator(Class<T> clazz, DocumentReference[] references, int limit) {
        super(clazz, limit);
        this.references = references;
    }

    @Override
    public void next(Consumer<List<Pair<String,T>>> onSuccess, Consumer<Exception> onFailure) {
        if (isEofReached()) {
            onFailure.accept(new EOFException());
            return;
        }else if (hasFailure()) {
            onFailure.accept(new IllegalStateException());
            return;
        }
        int currentPosition = position;
        List<Pair<String,T>> objects = new ArrayList<>();
        setLoading(true);

        for (; position - currentPosition < limit && position < references.length; position++) {
            DocumentReference document = references[position];

            document.get().
                    addOnSuccessListener(snapshot -> {
                        successes++;
                        objects.add(new Pair<>(document.getId(), Objects.requireNonNull(snapshot.toObject(clazz))));

                        if (objects.size() == limit || successes == references.length) {
                            onSuccess.accept(objects);
                            setLoading(false);
                        }
                    })
                    .addOnFailureListener(e->{
                        setFailed();
                        setLoading(false);
                        onFailure.accept(e);
                    })

            ;
        }

        if(position == references.length)
            setEofReached();
    }
}

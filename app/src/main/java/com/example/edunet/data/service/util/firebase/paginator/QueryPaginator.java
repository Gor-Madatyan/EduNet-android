package com.example.edunet.data.service.util.firebase.paginator;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import com.example.edunet.data.service.util.paginator.AbstractPaginator;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

public class QueryPaginator<T> extends AbstractPaginator<T> {
    private Query query;

    public QueryPaginator(@NonNull Query query, int limit, @NonNull Class<T> clazz) {
        super(clazz, limit);
        this.query = query.limit(limit);
    }

    @Override
    public void next(Consumer<List<Pair<String, T>>> onSuccess, Consumer<Exception> onFailure) {
        if (isEofReached()) {
            onFailure.accept(new EOFException());
            return;
        } else if (hasFailure() || isLoading()) {
            onFailure.accept(new IllegalStateException());
            return;
        }
        setLoading(true);
        query.get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        setEofReached();
                        onFailure.accept(new EOFException());
                        return;
                    }
                    List<Pair<String, T>> objects = new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot : snapshots) {
                        objects.add(new Pair<>(snapshot.getId(), snapshot.toObject(clazz)));
                    }

                    query = query.startAfter(snapshots.getDocuments().get(snapshots.size() - 1));
                    onSuccess.accept(objects);
                })
                .addOnFailureListener(e -> {
                    setFailed();
                    onFailure.accept(e);
                })
                .addOnCompleteListener(r -> setLoading(false));
    }

}

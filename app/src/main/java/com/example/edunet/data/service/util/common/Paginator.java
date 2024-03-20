package com.example.edunet.data.service.util.common;

import androidx.core.util.Consumer;

import com.example.edunet.data.service.util.firebase.FirestoreUtils;

import java.io.EOFException;
import java.util.List;

public interface Paginator<T> {
    /**
     * @param onSuccess called on success
     * @param onFailure called on failure, may receive {@link EOFException} when result is empty(no items remain),
     *                  it may be checked with {@link FirestoreUtils.Paginator#isEofReached()}, also it can receive {@link IllegalStateException}
     *                  if already once error occurred, it may be checked with {@link FirestoreUtils.Paginator#hasFailure()}.
     */
    void next(Consumer<List<T>> onSuccess, Consumer<Exception> onFailure);

    boolean hasFailure();

    boolean isEofReached();
}

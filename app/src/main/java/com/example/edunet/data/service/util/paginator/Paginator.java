package com.example.edunet.data.service.util.paginator;

import androidx.core.util.Consumer;

import com.example.edunet.data.service.util.firebase.paginator.QueryPaginator;

import java.io.EOFException;
import java.util.List;

public interface Paginator<T> {
    /**
     * @param onSuccess called on success
     * @param onFailure called on failure, may receive {@link EOFException} when result is empty(no items remain),
     *                  it may be checked with {@link QueryPaginator#isEofReached()}, also it can receive {@link IllegalStateException}
     *                  if already once error occurred, it may be checked with {@link QueryPaginator#hasFailure()}.
     */
    void next(Consumer<List<T>> onSuccess, Consumer<Exception> onFailure);

    boolean isLoading();

    boolean hasFailure();

    boolean isEofReached();
}

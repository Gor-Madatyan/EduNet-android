package com.example.edunet.data.service.util.common;

import androidx.core.util.Pair;

public abstract class AbstractPaginator<T> implements Paginator<Pair<String,T>> {
    protected final Class<T> clazz;
    protected final int limit;
    private boolean eof;
    private boolean failed;

    protected AbstractPaginator(Class<T> clazz, int limit) {
        this.clazz = clazz;
        this.limit = limit;
    }

    @Override
    public boolean hasFailure() {
        return failed;
    }

    @Override
    public boolean isEofReached() {
        return eof;
    }

    protected void setFailed() {
        failed = true;
    }

    protected void setEofReached() {
        eof = true;
    }
}

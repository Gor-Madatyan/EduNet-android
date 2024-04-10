package com.example.edunet.ui.util.adapter.util;

import static com.example.edunet.ui.util.adapter.util.ListAdapterUtils.addAll;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.util.paginator.Paginator;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.io.EOFException;

public final class LazyAdapterUtils {
    private static final String TAG = LazyAdapterUtils.class.getSimpleName();

    public enum InsertionPlace {
        START,
        END
    }

    private LazyAdapterUtils() {
    }

    public static <T> boolean isLoadingValid(int position, ListAdapter<? extends RecyclerView.ViewHolder,T> adapter, Paginator<T> paginator) {
        return !paginator.isLoading() && position > adapter.getDataset().size() - 11 && !paginator.hasFailure() && !paginator.isEofReached();
    }

    public static <T> void load(ListAdapter<? extends RecyclerView.ViewHolder,T> adapter, Paginator<T> paginator) {
        paginator.next(
                list -> addAll(adapter, list),
                e -> {
                    if (!(e.getCause() instanceof EOFException))
                        Log.e(TAG, e.toString());
                }
        );

    }

    public static <T> void loadIfAvailable(ListAdapter<? extends RecyclerView.ViewHolder ,T> adapter, int position, Paginator<T> paginator) {
        if (isLoadingValid(position, adapter, paginator))
            load(adapter, paginator);
    }

}

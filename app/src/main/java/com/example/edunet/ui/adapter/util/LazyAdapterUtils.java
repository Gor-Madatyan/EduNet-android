package com.example.edunet.ui.adapter.util;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.util.common.Paginator;

import java.io.EOFException;
import java.util.List;

public final class LazyAdapterUtils {
    private static final String TAG = LazyAdapterUtils.class.getSimpleName();

    public enum InsertionPlace {
        START,
        END
    }

    private LazyAdapterUtils() {
    }

    public static <T> boolean isLoadingValid(int position, List<T> dataSet, Paginator<T> paginator) {
        return !paginator.isLoading() && position > dataSet.size() - 11 && !paginator.hasFailure() && !paginator.isEofReached();
    }

    public static <T, VH extends RecyclerView.ViewHolder> void load(RecyclerView.Adapter<VH> adapter, Paginator<T> paginator, List<T> dataSet) {
        paginator.next(
                list -> addAll(adapter, dataSet, list),
                e -> {
                    if (!(e.getCause() instanceof EOFException))
                        Log.e(TAG, e.toString());
                }
        );

    }

    public static <T, VH extends RecyclerView.ViewHolder> void loadIfAvailable(RecyclerView.Adapter<VH> adapter, int position, List<T> dataSet, Paginator<T> paginator) {
        if (isLoadingValid(position, dataSet, paginator))
            load(adapter, paginator, dataSet);
    }

    public static <T, VH extends RecyclerView.ViewHolder> void addAll(RecyclerView.Adapter<VH> adapter, List<T> dataSet, List<T> list) {
        dataSet.addAll(list);
        adapter.notifyItemRangeInserted((dataSet.size() - 1) - (list.size() - 1), list.size());

    }

    public static <T, VH extends RecyclerView.ViewHolder> void addAll(RecyclerView.Adapter<VH> adapter, List<T> dataSet, List<T> list, InsertionPlace place) {
        if (place == InsertionPlace.END)
            addAll(adapter, dataSet, list);
        else if (place == InsertionPlace.START) {
            dataSet.addAll(0, list);
            adapter.notifyItemRangeInserted(0, list.size());
        }
    }
}

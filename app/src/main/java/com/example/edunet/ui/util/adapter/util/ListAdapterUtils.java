package com.example.edunet.ui.util.adapter.util;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;

public final class ListAdapterUtils {
    private ListAdapterUtils() {
    }


    public static <T> void addAll(ListAdapter<? extends RecyclerView.ViewHolder, T> adapter, List<T> list) {
        List<T> dataset = adapter.getDataset();
        dataset.addAll(list);
        adapter.notifyItemRangeInserted((dataset.size() - 1) - (list.size() - 1), list.size());

    }

    public static <T> void addAll(ListAdapter<? extends RecyclerView.ViewHolder, T> adapter, List<T> list, LazyAdapterUtils.InsertionPlace place) {
        if (place == LazyAdapterUtils.InsertionPlace.END)
            addAll(adapter, list);
        else if (place == LazyAdapterUtils.InsertionPlace.START) {
            adapter.getDataset().addAll(0, list);
            adapter.notifyItemRangeInserted(0, list.size());
        }
    }

    public static void remove(ListAdapter<? extends RecyclerView.ViewHolder, ?> adapter, int position) {
        adapter.getDataset().remove(position);
        adapter.notifyItemRemoved(position);
    }

}

package com.example.edunet.ui.util.adapter.util;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.util.selector.Selector;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;
import java.util.stream.IntStream;

public class SelectableAdapterUtils {
    private SelectableAdapterUtils() {
    }

    public static void toggle(int position, Selector<Integer> selector, RecyclerView.Adapter<?> adapter) {
        if (!selector.isSelected(position))
            select(position, selector, adapter);
        else unselect(position, selector, adapter);
    }

    public static void select(int position, Selector<Integer> selector, RecyclerView.Adapter<?> adapter) {
        selector.select(position);
        adapter.notifyItemChanged(position);
    }

    public static void selectAll(Selector<Integer> selector, ListAdapter<?, ?> adapter) {
        List<Integer> selected = selector.getSelections();
        selector.selectAll(IntStream.range(0, adapter.getDataset().size()).iterator());
        for (int i = 0; i < adapter.getDataset().size(); i++) {
            if (!selected.contains(i))
                adapter.notifyItemChanged(i);
        }
    }

    public static void unselect(int position, Selector<Integer> selector, RecyclerView.Adapter<?> adapter) {
        selector.unselect(position);
        adapter.notifyItemChanged(position);
    }

    public static void reset(Selector<Integer> selector, RecyclerView.Adapter<?> adapter) {
        List<Integer> selected = selector.getSelections();
        selector.reset();
        for (int position : selected) {
            adapter.notifyItemChanged(position);
        }
    }
}

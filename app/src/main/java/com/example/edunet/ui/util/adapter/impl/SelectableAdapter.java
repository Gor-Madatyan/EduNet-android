package com.example.edunet.ui.util.adapter.impl;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.util.selector.Selector;
import com.example.edunet.ui.util.SelectionUtils;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;

public class SelectableAdapter<VH extends RecyclerView.ViewHolder, T> extends ListAdapter<VH, T> {
    private final ListAdapter<VH, T> in;
    private final Selector<Integer> selector;

    public SelectableAdapter(@NonNull ListAdapter<VH, T> in, Selector<Integer> selector) {
        this.in = in;
        this.selector = selector;
    }

    @Override
    public List<T> getDataset() {
        return in.getDataset();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return in.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        in.onBindViewHolder(holder, position);
        SelectionUtils.bind(holder.itemView, selector.isSelected(position));
    }

    @Override
    public int getItemViewType(int position) {
        return in.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return getDataset().size();
    }
}

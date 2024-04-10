package com.example.edunet.ui.util.adapter.impl;

import static com.example.edunet.ui.util.adapter.util.LazyAdapterUtils.load;
import static com.example.edunet.ui.util.adapter.util.LazyAdapterUtils.loadIfAvailable;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.util.paginator.Paginator;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;

public class LazyAdapter<VH extends RecyclerView.ViewHolder, T> extends ListAdapter<VH,T> {
    private final ListAdapter<VH, T> in;
    private final Paginator<T> paginator;

    public LazyAdapter(ListAdapter<VH, T> in, Paginator<T> paginator) {
        this.in = in;
        this.paginator = paginator;
        load(this,paginator);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return in.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        in.onBindViewHolder(holder, position);
        loadIfAvailable(this, position, paginator);
    }

    @Override
    public int getItemViewType(int position) {
        return in.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return in.getItemCount();
    }

    @Override
    public List<T> getDataset() {
        return in.getDataset();
    }
}

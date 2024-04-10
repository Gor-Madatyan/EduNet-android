package com.example.edunet.ui.util.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class ListAdapter<VH extends RecyclerView.ViewHolder,T> extends RecyclerView.Adapter<VH> {
    public abstract List<T> getDataset();
}

package com.example.edunet.ui.util.adapter.impl;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.model.Entity;

public class EntityAdapterCallbackData<T extends Entity> {
    T entity;
    private final RecyclerView.ViewHolder viewHolder;

    public EntityAdapterCallbackData(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public View getView(){return  viewHolder.itemView;}

    public int getPosition() {
        return viewHolder.getAdapterPosition();
    }

    public T getEntity() {
        return entity;
    }

}

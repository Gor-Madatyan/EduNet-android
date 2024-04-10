package com.example.edunet.ui.util.adapter.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.model.Entity;
import com.example.edunet.ui.util.EntityUtils;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;
import java.util.function.BiConsumer;


public class EntityAdapter<T extends Entity> extends ListAdapter<EntityAdapter<T>.ViewHolder,T> {
    @LayoutRes
    private final int itemLayout;
    protected final List<T> dataSet;
    private final BiConsumer<View, CallbackData> callback;

    @Override
    public List<T> getDataset() {
        return dataSet;
    }

    public class CallbackData {
        private T entity;
        private final ViewHolder viewHolder;

        public CallbackData(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        public int getPosition() {
            return viewHolder.getAdapterPosition();
        }

        public T getEntity() {
            return entity;
        }
    }

    public EntityAdapter(List<T> dataSet, @LayoutRes int itemLayout, BiConsumer<View, CallbackData> callback) {
        this.dataSet = dataSet;
        this.itemLayout = itemLayout;
        this.callback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CallbackData callbackData = new CallbackData(this);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callback.accept(itemView, callbackData);
        }

        public void setEntity(@NonNull T entity) {
            callbackData.entity = entity;
            EntityUtils.bindNameAvatarElement(entity, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemLayout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T entity = dataSet.get(position);
        holder.setEntity(entity);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

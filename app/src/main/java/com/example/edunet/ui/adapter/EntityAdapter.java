package com.example.edunet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.data.service.model.Entity;
import com.example.edunet.ui.util.EntityUtils;

import java.util.List;
import java.util.function.BiConsumer;


public class EntityAdapter<T extends Entity> extends RecyclerView.Adapter<EntityAdapter<T>.ViewHolder> {
    @LayoutRes
    private final int itemLayout;
    protected final List<T> dataSet;
    private final BiConsumer<View, CallbackData> callback;

    public class CallbackData {
        private T entity;
        private int position;

        public int getPosition() {
            return position;
        }
        public T getEntity(){
            return entity;
        }
    }

    public EntityAdapter(List<T> dataSet, @LayoutRes int itemLayout, BiConsumer<View, CallbackData> callback) {
        this.dataSet = dataSet;
        this.itemLayout = itemLayout;
        this.callback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CallbackData callbackData = new CallbackData();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callback.accept(itemView, callbackData);
        }

        public void setEntity(int position, @NonNull T entity) {
            callbackData.position = position;
            callbackData.entity = entity;
            EntityUtils.bindNameAvatarElement(entity, itemView);
        }
    }
    public void deleteItem(int position){
        dataSet.remove(position);
        notifyItemRemoved(position);
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
        holder.setEntity(position, entity);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

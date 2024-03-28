package com.example.edunet.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.common.util.UriUtils;
import com.example.edunet.data.service.model.Entity;
import com.example.edunet.ui.util.EntityUtils;

import java.util.List;
import java.util.function.BiConsumer;


public class EntityAdapter<T extends Entity> extends RecyclerView.Adapter<EntityAdapter<T>.ViewHolder> {
    @LayoutRes
    private final int itemLayout;
    protected final List<Pair<String, T>> dataSet;
    private final BiConsumer<View, CallbackData> callback;

    public static class CallbackData {
        private String title;
        private Uri avatar;
        private String id;
        private int position;

        public int getPosition() {
            return position;
        }
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Uri getAvatar() {
            return avatar;
        }
    }

    public EntityAdapter(List<Pair<String, T>> dataSet, @LayoutRes int itemLayout, BiConsumer<View, CallbackData> callback) {
        this.dataSet = dataSet;
        this.itemLayout = itemLayout;
        this.callback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final EntityAdapter.CallbackData callbackData = new CallbackData();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callback.accept(itemView, callbackData);
        }

        public void setEntity(int position, @NonNull String id, @NonNull T entity) {
            callbackData.id = id;
            callbackData.position = position;
            callbackData.title = entity.getName();
            callbackData.avatar = UriUtils.safeParse(entity.getAvatar());
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
        Pair<String, T> entity = dataSet.get(position);
        holder.setEntity(position, entity.first, entity.second);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

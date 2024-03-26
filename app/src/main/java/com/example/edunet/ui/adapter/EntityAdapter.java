package com.example.edunet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.R;
import com.example.edunet.common.util.UriUtils;
import com.example.edunet.data.service.model.Entity;
import com.example.edunet.ui.util.ImageLoadingUtils;

import java.util.List;
import java.util.function.BiConsumer;


public class EntityAdapter<T extends Entity> extends RecyclerView.Adapter<EntityAdapter<T>.ViewHolder> {
    @DrawableRes
    private final int defaultAvatar;
    @LayoutRes
    private final int itemLayout;
    protected final List<Pair<String, T>> dataSet;
    private final BiConsumer<View, CallbackData> callback;

    public static class CallbackData {
        private String id;
        private int position;

        public int getPosition() {
            return position;
        }
        public String getId() {
            return id;
        }
    }

    public EntityAdapter(List<Pair<String, T>> dataSet, @LayoutRes int itemLayout, @DrawableRes int defaultAvatar, BiConsumer<View, CallbackData> callback) {
        this.dataSet = dataSet;
        this.itemLayout = itemLayout;
        this.defaultAvatar = defaultAvatar;
        this.callback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final EntityAdapter.CallbackData callbackData = new CallbackData();
        private final ImageView avatar;
        private final TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            callback.accept(itemView, callbackData);
        }

        public void setEntity(int position, @NonNull String id, @NonNull T entity) {
            callbackData.id = id;
            callbackData.position = position;
            name.setText(entity.getName());
            ImageLoadingUtils.loadAvatar(
                    itemView,
                    UriUtils.safeParse(entity.getAvatar()),
                    defaultAvatar,
                    avatar);
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

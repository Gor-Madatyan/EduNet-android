package com.example.edunet.ui.util.adapter.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.R;
import com.example.edunet.data.service.model.User;
import com.example.edunet.ui.util.EntityUtils;
import com.example.edunet.ui.util.TextUtils;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;

public class GraduationAdapter extends ListAdapter<GraduationAdapter.ViewHolder, Object> {
    @LayoutRes
    private final int itemLayout;
    protected final List<Object> dataset;
    private final Consumer<EntityAdapterCallbackData<User>> callback;

    @Override
    public List<Object> getDataset() {
        return dataset;
    }

    public GraduationAdapter(@NonNull List<Object> dataSet, @LayoutRes int itemLayout,@NonNull Consumer<EntityAdapterCallbackData<User>> callback) {
        this.dataset = dataSet;
        this.itemLayout = itemLayout;
        this.callback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EntityAdapterCallbackData<User> callbackData;
        public ViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            if(type == itemLayout) {
                callbackData = new EntityAdapterCallbackData<>(this);
                callback.accept(callbackData);
            }
        }

        public void set(int position) {
            Object item = dataset.get(position);

            if (getItemViewType() == R.layout.header)
                TextUtils.bindHeader((String) item, itemView);
            else {
                User user = (User) item;
                callbackData.entity = user;
                EntityUtils.bindNameAvatarElement(user, itemView);
            }
        }
    }

    @NonNull
    @Override
    public GraduationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull GraduationAdapter.ViewHolder holder, int position) {
        holder.set(position);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = dataset.get(position);
        if (item instanceof String)
            return R.layout.header;
        else if (item instanceof User)
            return itemLayout;

        throw new ClassCastException();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

}
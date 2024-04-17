package com.example.edunet.ui.util.adapter.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
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

    @Override
    public List<Object> getDataset() {
        return dataset;
    }

    public GraduationAdapter(List<Object> dataSet, @LayoutRes int itemLayout) {
        this.dataset = dataSet;
        this.itemLayout = itemLayout;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void set(int position) {
            Object object = dataset.get(position);

            if (getItemViewType() == R.layout.header)
                TextUtils.bindHeader((String) object, itemView);
            else
                EntityUtils.bindNameAvatarElement((User) object, itemView);
        }
    }

    @NonNull
    @Override
    public GraduationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new ViewHolder(view);
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
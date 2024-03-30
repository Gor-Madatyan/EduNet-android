package com.example.edunet.ui.adapter;

import static com.example.edunet.ui.adapter.util.LazyAdapterUtils.load;
import static com.example.edunet.ui.adapter.util.LazyAdapterUtils.loadIfAvailable;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.edunet.data.service.model.Entity;
import com.example.edunet.data.service.util.common.Paginator;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class LazyEntityAdapter<T extends Entity> extends EntityAdapter<T> {
    private final Paginator<Pair<String, T>> paginator;

    public LazyEntityAdapter(Paginator<Pair<String, T>> paginator, @LayoutRes int itemLayout, BiConsumer<View, CallbackData> callBack) {
        super(new ArrayList<>(), itemLayout, callBack);
        this.paginator = paginator;
        load(this,paginator,dataSet);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        loadIfAvailable(this,position,dataSet,paginator);
    }
}

package com.example.edunet.ui.adapter;

import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.edunet.data.service.model.Entity;
import com.example.edunet.data.service.util.common.Paginator;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class LazyEntityAdapter<T extends Entity> extends EntityAdapter<T> {
    private static final String TAG = LazyEntityAdapter.class.getSimpleName();
    private final Paginator<Pair<String, T>> paginator;
    private boolean isLoading = false;

    public LazyEntityAdapter(Paginator<Pair<String, T>> paginator, @LayoutRes int itemLayout, @DrawableRes int defaultAvatar, BiConsumer<View, CallbackData> callBack) {
        super(new ArrayList<>(), itemLayout, defaultAvatar, callBack);
        this.paginator = paginator;
        load();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!isLoading && position > dataSet.size() - 11 && !paginator.hasFailure() && !paginator.isEofReached())
            load();
    }

    private void load(){
        isLoading = true;

        paginator.next(
                this::onSuccess,
                this::onFailure
        );
    }

    private void onFailure(Exception e){
        isLoading = false;
        if (!(e.getCause() instanceof EOFException))
            Log.e(TAG, e.toString());
    }

    private void onSuccess(List<Pair<String, T>> list) {
        isLoading = false;
        dataSet.addAll(list);
        notifyItemRangeInserted((dataSet.size() - 1) - (list.size() - 1), list.size());
    }
}

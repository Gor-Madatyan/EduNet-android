package com.example.edunet.ui.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.util.common.Paginator;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

public class LazyCommunityAdapter extends CommunityAdapter {
    private static final String TAG = LazyCommunityAdapter.class.getSimpleName();
    private final Paginator<Pair<String, Community>> paginator;
    private boolean isLoading = false;

    public LazyCommunityAdapter(Paginator<Pair<String, Community>> paginator, Consumer<String> callBack) {
        super(new ArrayList<>(), callBack);
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

    private void onSuccess(List<Pair<String, Community>> list) {
        isLoading = false;
        dataSet.addAll(list);
        notifyItemRangeInserted((dataSet.size() - 1) - (list.size() - 1), list.size());
    }
}

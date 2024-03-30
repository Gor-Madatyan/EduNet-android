package com.example.edunet.ui.adapter;

import static com.example.edunet.ui.adapter.util.LazyAdapterUtils.addAll;
import static com.example.edunet.ui.adapter.util.LazyAdapterUtils.load;
import static com.example.edunet.ui.adapter.util.LazyAdapterUtils.loadIfAvailable;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.MessagingService;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.util.common.Paginator;
import com.example.edunet.ui.adapter.util.LazyAdapterUtils;

import java.util.ArrayList;
import java.util.List;

public class LazyMessageAdapter extends MessageAdapter {
    private final Paginator<Pair<String, Message>> paginator;

    public LazyMessageAdapter(Paginator<Pair<String,Message>> paginator, AccountService accountService, MessagingService messagingService) {
        super(new ArrayList<>(), accountService, messagingService);
        this.paginator = paginator;
        load(this, paginator, dataSet);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        loadIfAvailable(this, position, dataSet, paginator);
    }

    public void addNewMessages(List<Pair<String, Message>> messages){
        addAll(this, dataSet, messages, LazyAdapterUtils.InsertionPlace.START);
    }

}

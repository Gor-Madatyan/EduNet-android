package com.example.edunet.ui.adapter;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.MessagingService;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.ui.util.EntityUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    private final AccountService accountService;
    private final MessagingService messagingService;
    protected final List<Message> dataSet;

    public MessageAdapter(List<Message> dataSet, AccountService accountService, MessagingService messagingService) {
        this.dataSet = dataSet;
        this.accountService = accountService;
        this.messagingService = messagingService;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView body;
        private final TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.message);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

        void setMessage(Message message) {
            int viewType = getItemViewType();

            body.setText(message.message());
            timestamp.setText(DateFormat.getDateFormat(itemView.getContext()).format(message.date()));

            if (viewType == R.layout.foreign_message) accountService.getUserById(message.senderId(),
                    user ->
                            EntityUtils.bindNameAvatarElement(user, itemView),
                    e ->
                            Log.e(TAG, "cant load sender data")
            );
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setMessage(dataSet.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Message message = dataSet.get(position);
        return messagingService.isCurrentUserOwner(message) ? R.layout.user_message : R.layout.foreign_message;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

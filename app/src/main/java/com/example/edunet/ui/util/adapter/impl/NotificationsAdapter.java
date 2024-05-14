package com.example.edunet.ui.util.adapter.impl;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.R;
import com.example.edunet.data.service.model.UserNotification;
import com.example.edunet.data.service.model.UserOperation;
import com.example.edunet.ui.util.adapter.ListAdapter;

import java.util.List;

public class NotificationsAdapter extends ListAdapter<NotificationsAdapter.ViewHolder, UserNotification> {
    private final List<UserNotification> userNotifications;
    @LayoutRes
    private final int itemLayout;

    public NotificationsAdapter(@NonNull List<UserNotification> userNotifications, @LayoutRes int itemLayout) {
        super();
        this.userNotifications = userNotifications;
        this.itemLayout = itemLayout;
    }

    @Override
    public List<UserNotification> getDataset() {
        return userNotifications;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView body;
        private final TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            timestamp = itemView.findViewById(R.id.timestamp);
        }

        public void setUserNotification(UserNotification userNotification) {
            title.setText(userNotification.operationType() == UserOperation.ADDED ? R.string.users_added : R.string.users_removed);
            body.setText(itemView.getContext().getString(
                    userNotification.operationType() == UserOperation.ADDED ? R.string.users_added_message : R.string.users_removed_message,
                    userNotification.users().length, userNotification.arePending() ? "PENDING" : "", userNotification.membersType()));
            timestamp.setText(DateFormat.getDateFormat(itemView.getContext()).format(userNotification.date()));
        }
    }

    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(itemLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int position) {
        holder.setUserNotification(userNotifications.get(position));
    }

    @Override
    public int getItemCount() {
        return userNotifications.size();
    }
}

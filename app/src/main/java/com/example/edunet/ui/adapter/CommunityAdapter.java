package com.example.edunet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunet.MainNavDirections;
import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    private final Pair<String, Community>[] dataSet;

    public CommunityAdapter(Pair<String, Community>[] dataSet) {
        this.dataSet = dataSet;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private String communityId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                        MainNavDirections.ActionGlobalCommunityFragment action = MainNavDirections.actionGlobalCommunityFragment(communityId);
                        Navigation.findNavController(v).navigate(action);
                    }
            );
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        public void setCommunity(@NonNull String communityId, @NonNull Community community) {
            this.communityId = communityId;
            name.setText(community.getName());
            Glide.with(itemView)
                    .load(community.getAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_group)
                    .into(avatar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.community_element, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String,Community> community = dataSet[position];
        holder.setCommunity(community.first,community.second);
    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }

}

package com.example.edunet.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.ui.util.ImageLoadingUtils;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    protected final List<Pair<String, Community>> dataSet;
    private final Consumer<String> callBack;

    public CommunityAdapter(List<Pair<String, Community>> dataSet, Consumer<String> callBack) {
        this.dataSet = dataSet;
        this.callBack = callBack;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private String communityId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> callBack.accept(communityId)/*{
                        MainNavDirections.ActionGlobalCommunityFragment action = MainNavDirections.actionGlobalCommunityFragment(communityId);
                        Navigation.findNavController(v).navigate(action);
                    }*/
            );
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        public void setCommunity(@NonNull String communityId, @NonNull Community community) {
            this.communityId = communityId;
            name.setText(community.getName());
            ImageLoadingUtils.loadCommunityAvatar(
                    itemView,
                    community.getAvatar() == null ? null : Uri.parse(community.getAvatar()),
                    avatar);
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
        Pair<String, Community> community = dataSet.get(position);
        holder.setCommunity(community.first, community.second);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

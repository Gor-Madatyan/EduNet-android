package com.example.edunet.ui.screen.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.edunet.R;
import com.example.edunet.databinding.FragmentCommunityBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommunityFragment extends Fragment {
    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String communityId = CommunityFragmentArgs.fromBundle(getArguments()).getCommunityId();

        viewModel.setCommunity(this,communityId);

        viewModel.uiState.observe(getViewLifecycleOwner(),state->{
            binding.toolbarLayout.setTitle(state.community().getName());
            binding.description.setText(state.community().getDescription());
            Glide.with(this)
                    .load(state.community().getAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_group)
                    .into(binding.avatar);
        });

    }
}
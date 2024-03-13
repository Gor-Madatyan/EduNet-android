package com.example.edunet.ui.screen.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.databinding.FragmentCommunityBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommunityFragment extends Fragment {
    private NavController navController;
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
        navController = Navigation.findNavController(view);
        String communityId = CommunityFragmentArgs.fromBundle(getArguments()).getCommunityId();
        viewModel.observeCommunity(getViewLifecycleOwner(), communityId);

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            if (state.error() != null) {
                Toast.makeText(requireContext().getApplicationContext(), R.string.error_cant_load_community, Toast.LENGTH_LONG).show();
                navController.navigateUp();
                return;
            }

            if (state.isCurrentUserOwner()) {
                MenuItem edit = binding.toolbar.getMenu().getItem(0);
                edit.setVisible(true);
                edit.setOnMenuItemClickListener(i -> {
                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToCommunityUpdateFragment(state.community(), communityId));
                    return true;
                });

                Community community = state.community();

                binding.toolbarLayout.setTitle(community.getName());
                binding.description.setText(community.getDescription());
                Glide.with(this)
                        .load(state.community().getAvatar())
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_group)
                        .into(binding.avatar);
            }
        });

    }
}
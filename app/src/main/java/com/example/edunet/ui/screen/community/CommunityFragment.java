package com.example.edunet.ui.screen.community;

import android.net.Uri;
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

import com.example.edunet.MainNavDirections;
import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.databinding.FragmentCommunityBinding;
import com.example.edunet.ui.adapter.CommunityAdapter;
import com.example.edunet.ui.util.ImageLoadingUtils;

import java.util.Arrays;

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

            if (state.subCommunities().length > 0) {
                binding.subcommunitiesContainer.setVisibility(View.VISIBLE);
                binding.subcommunities.setAdapter(new CommunityAdapter(Arrays.asList(state.subCommunities()), id -> {
                    MainNavDirections.ActionGlobalCommunityFragment action = MainNavDirections.actionGlobalCommunityFragment(id);
                    navController.navigate(action);
                }));

            } else binding.subcommunitiesContainer.setVisibility(View.GONE);

            if (state.isCurrentUserOwner()) {
                MenuItem edit = binding.toolbar.getMenu().getItem(0);
                MenuItem addSubCommunity = binding.toolbar.getMenu().getItem(2);
                edit.setVisible(true);
                addSubCommunity.setVisible(true);

                edit.setOnMenuItemClickListener(i -> {
                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToCommunityUpdateFragment(state.community(), communityId));
                    return true;
                });

                addSubCommunity.setOnMenuItemClickListener(i -> {
                            navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToAddCommunityFragment(communityId));
                            return true;
                        }
                );
                MenuItem delete = binding.toolbar.getMenu().getItem(1);

                if (state.subCommunities().length == 0) {
                    delete.setVisible(true);
                    delete.setOnMenuItemClickListener(i -> {
                        navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToCommunityDeleteDialogFragment(communityId));
                        return true;
                    });
                } else delete.setVisible(false);
            }
            Community community = state.community();

            if (community != null) {
                String avatar = community.getAvatar();
                binding.toolbarLayout.setTitle(community.getName());
                binding.description.setText(community.getDescription());
                ImageLoadingUtils.loadCommunityAvatar(this, avatar == null ? null : Uri.parse(avatar), binding.avatar);
            }

        });

    }
}
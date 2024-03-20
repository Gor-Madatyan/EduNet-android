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

import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.databinding.FragmentCommunityBinding;
import com.example.edunet.ui.util.ImageLoadingUtils;

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
                MenuItem delete = binding.toolbar.getMenu().getItem(1);
                delete.setVisible(true);
                edit.setVisible(true);
                edit.setOnMenuItemClickListener(i -> {
                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToCommunityUpdateFragment(state.community(), communityId));
                    return true;
                });
                delete.setOnMenuItemClickListener(i -> {

                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToCommunityDeleteDialogFragment(communityId));
                    return true;
                });
            }
            Community community = state.community();
            String avatar = state.community().getAvatar();

            binding.toolbarLayout.setTitle(community.getName());
            binding.description.setText(community.getDescription());
            ImageLoadingUtils.loadCommunityAvatar(this, avatar == null ? null : Uri.parse(avatar), binding.avatar);

        });

    }
}
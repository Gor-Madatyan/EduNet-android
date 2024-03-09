package com.example.edunet.ui.screen.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.edunet.R;
import com.example.edunet.StartUpActivity;
import com.example.edunet.databinding.FragmentProfileBinding;
import com.example.edunet.ui.adapter.CommunityAdapter;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_sign_out) {
                viewModel.signOut();
                startActivity(new Intent(requireActivity(), StartUpActivity.class));
                requireActivity().finish();
                return true;
            } else if (id == R.id.action_edit_profile) {
                navController.navigate(R.id.action_navigation_profile_to_profileUpdateFragment);
                return true;
            }

            return false;
        });

        binding.addCommunity.setOnClickListener(v -> navController.navigate(R.id.action_navigation_profile_to_addCommunityFragment));

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            binding.toolbarLayout.setTitle(state.user().name());
            binding.bio.setText(Objects.requireNonNullElse(state.user().bio(), getString(R.string.default_bio)));
            Glide.with(this)
                    .load(state.user().photo())
                    .placeholder(R.drawable.ic_default_user)
                    .circleCrop()
                    .into(binding.avatar);
            if(state.user().ownedCommunities().length > 0) {
                binding.ownedCommunitiesContainer.setVisibility(View.VISIBLE);
                binding.ownedCommunities.setAdapter(new CommunityAdapter(state.user().ownedCommunities()));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
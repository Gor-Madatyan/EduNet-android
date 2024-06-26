package com.example.edunet.ui.screen.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunet.MainNavDirections;
import com.example.edunet.R;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.databinding.FragmentProfileBinding;
import com.example.edunet.ui.util.CredentialUtils;
import com.example.edunet.ui.util.ImageLoadingUtils;
import com.example.edunet.ui.util.adapter.impl.EntityAdapter;

import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private NavController navController;
    @Inject
    CredentialManager credentialManager;

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
        String uid = ProfileFragmentArgs.fromBundle(requireArguments()).getUserId();
        viewModel.setUser(uid, getViewLifecycleOwner());
        viewModel.observeAttachedCommunities(getViewLifecycleOwner());
        navController = Navigation.findNavController(view);

        if (viewModel.isUserCurrent()) {
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.addCommunity.setVisibility(View.VISIBLE);
        }


        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_sign_out) {
                viewModel.signOut();
                CredentialUtils.clearCredentials(credentialManager);
                navController.navigate(ProfileFragmentDirections.actionGlobalSignInFragment());
                return true;
            } else if (id == R.id.action_edit_profile) {
                navController.navigate(R.id.action_navigation_profile_to_profileUpdateFragment);
                return true;
            }

            return false;
        });

        binding.addCommunity.setOnClickListener(v -> {
            if(!viewModel.isCurrentUserEmailVerified()){
                Toast.makeText(requireContext(), R.string.email_not_verified, Toast.LENGTH_SHORT).show();
                return;
            }
            navController.navigate(ProfileFragmentDirections.actionNavigationProfileToAddCommunityFragment());
        });

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(state.user().getEmail());
            binding.toolbarLayout.setTitle(state.user().getName());
            binding.bio.setText(Objects.requireNonNullElse(state.user().getBio(), getString(R.string.default_bio)));
            ImageLoadingUtils.loadUserAvatar(this, state.user().getAvatar(), binding.avatar);
            
            if(Objects.requireNonNull(navController.getCurrentDestination()).getId() == R.id.navigation_profile && viewModel.isUserCurrent() && !viewModel.isCurrentUserEmailVerified())
                navController.navigate(ProfileFragmentDirections.actionNavigationProfileToVerifyEmailDialog());

            processAttachedCommunities(binding.ownedCommunitiesContainer, binding.ownedCommunities, state.ownedCommunities());
            processAttachedCommunities(binding.adminedCommunitiesContainer, binding.adminedCommunities, state.adminedCommunities());
            processAttachedCommunities(binding.participatedCommunitiesContainer, binding.participatedCommunities, state.participatedCommunities());
            processAttachedCommunities(binding.graduatedCommunitiesContainer, binding.graduatedCommunities, state.graduatedCommunities());

        });
    }

    private void processAttachedCommunities(ViewGroup container, RecyclerView recyclerView, Community[] communities) {
        if (communities.length > 0) {
            container.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new EntityAdapter<>(Arrays.asList(communities), R.layout.name_avatar_element, data ->
                    data.getView().setOnClickListener(
                            v -> {
                                MainNavDirections.ActionGlobalCommunityFragment action = MainNavDirections.actionGlobalCommunityFragment(data.getEntity().getId());
                                navController.navigate(action);
                            })
            ));
        } else
            container.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
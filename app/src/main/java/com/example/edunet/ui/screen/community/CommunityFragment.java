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
import com.example.edunet.data.service.model.Role;
import com.example.edunet.databinding.FragmentCommunityBinding;
import com.example.edunet.ui.util.EntityUtils;
import com.example.edunet.ui.util.ImageLoadingUtils;
import com.example.edunet.ui.util.adapter.impl.EntityAdapter;
import com.example.edunet.ui.util.viewmodel.CommunityViewModel;

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

        MenuItem adminPanel = binding.toolbar.getMenu().findItem(R.id.action_admin_panel);
        MenuItem addSubCommunity = binding.toolbar.getMenu().findItem(R.id.action_add_subcommunity);
        MenuItem requestParticipantPermissions = binding.toolbar.getMenu().findItem(R.id.action_request_participant_permissions);
        MenuItem requestAdminPermissions = binding.toolbar.getMenu().findItem(R.id.action_request_admin_permissions);

        adminPanel.setOnMenuItemClickListener(i -> {
            navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToAdminPanelFragment(communityId));
            return true;
        });
        requestAdminPermissions.setOnMenuItemClickListener(i -> {
                    if (viewModel.getRole() == Role.UNVERIFIED_GUEST) {
                        Toast.makeText(requireContext().getApplicationContext(), R.string.email_not_verified, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToAdminPermissionRequestDialog(communityId));
                    return true;
                }
        );
        requestParticipantPermissions.setOnMenuItemClickListener(i -> {
                    if (viewModel.getRole() == Role.UNVERIFIED_GUEST) {
                        Toast.makeText(requireContext().getApplicationContext(), R.string.email_not_verified, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToParticipantPermissionRequestDialog(communityId));
                    return true;
                }
        );
        addSubCommunity.setOnMenuItemClickListener(i -> {
            navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToAddCommunityFragment(communityId));
            return true;
        });

        binding.viewGraduates.setOnClickListener(v ->
                navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToGraduationsFragment(communityId))
        );
        binding.viewParticipants.setOnClickListener(v ->
                navController.navigate(CommunityFragmentDirections.actionCommunityFragmentToMembersFragment(communityId, Role.PARTICIPANT))
        );

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            if (state.error() != null) {
                Toast.makeText(requireContext().getApplicationContext(), R.string.error_cant_load_community, Toast.LENGTH_LONG).show();
                navController.navigateUp();
                return;
            }

            if (state.subCommunities().length > 0) {
                binding.subcommunitiesContainer.setVisibility(View.VISIBLE);
                binding.subcommunities.setAdapter(new EntityAdapter<>(Arrays.asList(state.subCommunities()), R.layout.name_avatar_element, data ->
                        data.getView().setOnClickListener(v -> {
                            MainNavDirections.ActionGlobalCommunityFragment action = MainNavDirections.actionGlobalCommunityFragment(data.getEntity().getId());
                            navController.navigate(action);
                        })));

            } else binding.subcommunitiesContainer.setVisibility(View.GONE);

            adminPanel.setVisible(state.role() == Role.ADMIN || state.role() == Role.OWNER);
            if ((state.role() == Role.GUEST || state.role() == Role.UNVERIFIED_GUEST) &&
                    !state.isCurrentUserRequestedParticipantPermissions() && !state.isCurrentUserRequestedAdminPermissions()) {
                requestAdminPermissions.setVisible(true);
                requestParticipantPermissions.setVisible(true);
            } else {
                requestAdminPermissions.setVisible(false);
                requestParticipantPermissions.setVisible(false);
            }
            Community community = state.community();
            Community ancestor = state.superCommunity();

            if (community != null) {
                addSubCommunity.setVisible(community.getGraduated().isEmpty() && state.role() == Role.OWNER);
                Uri avatar = community.getAvatar();
                binding.toolbarLayout.setTitle(community.getName());
                binding.description.setText(community.getDescription());
                ImageLoadingUtils.loadCommunityAvatar(this, avatar, binding.avatar);
            }

            if (ancestor != null) {
                assert community != null;
                binding.ancestorContainer.setVisibility(View.VISIBLE);
                EntityUtils.bindNameAvatarElement(ancestor, binding.ancestor.getRoot());
                binding.ancestor.getRoot().setOnClickListener(
                        v ->
                                navController.navigate(MainNavDirections.actionGlobalCommunityFragment(community.getAncestor()))

                );
            }

        });

    }
}
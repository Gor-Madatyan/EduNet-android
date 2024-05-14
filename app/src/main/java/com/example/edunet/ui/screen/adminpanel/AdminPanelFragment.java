package com.example.edunet.ui.screen.adminpanel;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.ui.util.viewmodel.CommunityViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminPanelFragment extends PreferenceFragmentCompat {
    CommunityViewModel viewModel;
    private NavController navController;
    public final static String IS_COMMUNITY_DESTROYED_KEY = "IS_COMMUNITY_DESTROYED";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        var args = AdminPanelFragmentArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        viewModel.observeCommunity(getViewLifecycleOwner(), communityId);
        Preference adminRequests = findPreference("admin_requests");
        Preference participantRequests = findPreference("participant_requests");
        Preference editCommunity = findPreference("edit_community");
        Preference deleteCommunity = findPreference("delete_community");
        Preference admins = findPreference("admins");
        Preference participants = findPreference("participants");
        Preference notifications = findPreference("notifications");

        assert deleteCommunity != null;
        assert adminRequests != null;
        assert participantRequests != null;
        assert editCommunity != null;
        assert admins != null;
        assert participants != null;
        assert notifications != null;


        SavedStateHandle savedStateHandle = navController.getBackStackEntry(R.id.adminPanelFragment).getSavedStateHandle();

        savedStateHandle.<Boolean>getLiveData(IS_COMMUNITY_DESTROYED_KEY).observe(
                getViewLifecycleOwner(),
                isDeleted -> {
                    savedStateHandle.remove(IS_COMMUNITY_DESTROYED_KEY);
                    if (isDeleted) navController.navigateUp();
                }
        );

        notifications.setOnPreferenceClickListener(p->{
            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToNotificationsFragment(communityId));
            return true;
        });

        admins.setOnPreferenceClickListener(p->{
            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToMembersFragment(communityId,Role.ADMIN));
            return true;
        });

        participants.setOnPreferenceClickListener(p->{
            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToMembersFragment(communityId,Role.PARTICIPANT));
            return true;
        });

        adminRequests.setOnPreferenceClickListener(p -> {
            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToRequestsFragment(communityId,Role.ADMIN));
            return true;
        });

        participantRequests.setOnPreferenceClickListener(p -> {
            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToRequestsFragment(communityId,Role.PARTICIPANT));
            return true;
        });

        deleteCommunity.setOnPreferenceClickListener(p -> {
                    navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToCommunityDeleteDialogFragment(communityId));
                    return true;
                }
        );

        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            if (state.role() == Role.OWNER) {
                adminRequests.setVisible(true);
                admins.setVisible(true);
                deleteCommunity.setVisible(state.subCommunities().length == 0);
            }

            if (state.community() != null)
                editCommunity.setOnPreferenceClickListener(p -> {
                            navController.navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToCommunityUpdateFragment(state.community()));
                            return true;
                        }
                );
        });
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fragment_admin_panel, rootKey);
    }
}
package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.task.community.CommunityTaskManager;
import com.example.edunet.data.service.util.work.WorkUtils;
import com.example.edunet.ui.screen.adminpanel.AdminPanelFragment;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommunityDeleteDialogFragment extends DialogFragment {

    private NavController navController;
    @Inject
    CommunityTaskManager communityTaskManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String communityId = CommunityDeleteDialogFragmentArgs.fromBundle(getArguments()).getCommunityId();
        navController = NavHostFragment.findNavController(this);
        SavedStateHandle previousSavedStateHandle = Objects.requireNonNull(navController.getPreviousBackStackEntry()).getSavedStateHandle();

        return new AlertDialog.Builder(requireContext())
                .setMessage(R.string.community_delete_dialog)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            WorkUtils.observe(requireContext().getApplicationContext(),
                                    communityTaskManager.startCommunityDeleteTask(communityId),
                                    R.string.error_cant_delete_community
                            );

                            navController.navigateUp();
                            previousSavedStateHandle.set(AdminPanelFragment.IS_COMMUNITY_DESTROYED_KEY, true);
                        }
                )
                .setNegativeButton(android.R.string.no, (dialog, which) ->
                        previousSavedStateHandle.set(AdminPanelFragment.IS_COMMUNITY_DESTROYED_KEY, false))
                .create();
    }

}

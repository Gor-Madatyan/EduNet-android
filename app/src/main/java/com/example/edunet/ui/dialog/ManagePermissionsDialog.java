package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.fragment.NavHostFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.ui.screen.adminpanel.requests.RequestsFragment;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagePermissionsDialog extends DialogFragment {
    private static final String TAG = ManagePermissionsDialog.class.getSimpleName();
    @Inject
    CommunityService communityService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        var args = ManagePermissionsDialogArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        String userId = args.getUserId();
        boolean accept = args.getAccept();
        Role role = args.getRole();
        SavedStateHandle previousSavedStateHandle = Objects.requireNonNull(NavHostFragment.findNavController(this).getPreviousBackStackEntry()).getSavedStateHandle();

        return new AlertDialog.Builder(requireContext())
                .setMessage(getMessage(accept, role))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            previousSavedStateHandle.set(RequestsFragment.IS_REQUEST_MANAGED_KEY, true);
                            communityService.managePermissions(role, accept, communityId, userId,
                                    e -> {
                                        if (e != null) {
                                            Log.w(TAG, e);
                                            Toast.makeText(requireContext().getApplicationContext(), R.string.error_cant_manage_permissions, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }

                )
                .setNegativeButton(android.R.string.no, (dialog, which) -> previousSavedStateHandle.set(RequestsFragment.IS_REQUEST_MANAGED_KEY, false))
                .create();
    }

    @StringRes
    private int getMessage(boolean accept, Role role) {
        if (role == Role.ADMIN)
            return accept ? R.string.accept_admin_dialog : R.string.decline_admin_dialog;
        if (role == Role.PARTICIPANT)
            return accept ? R.string.accept_participant_dialog : R.string.decline_participant_dialog;

        throw new AssertionError();
    }

}

package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.fragment.NavHostFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.ui.screen.adminpanel.members.MembersFragment;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeleteMemberDialog extends DialogFragment {
    private static final String TAG = DeleteMemberDialog.class.getSimpleName();
    @Inject
    CommunityService communityService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        SavedStateHandle previousSavedStateHandle = Objects.requireNonNull(NavHostFragment.findNavController(this).getPreviousBackStackEntry()).getSavedStateHandle();

        return new AlertDialog.Builder(requireContext())
                .setMessage(R.string.member_delete_dialog)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    previousSavedStateHandle.set(MembersFragment.IS_ITEM_DELETED_KEY, true);
                    delete(e -> {
                                if (e != null) {
                                    Log.w(TAG, e);
                                    Toast.makeText(requireContext().getApplicationContext(), R.string.error_cant_delete_member, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                )
                .setNegativeButton(android.R.string.no, (dialog, which) -> previousSavedStateHandle.set(MembersFragment.IS_ITEM_DELETED_KEY, false))
                .create();
    }

    private void delete(Consumer<Exception> onResult) {
        var args = DeleteMemberDialogArgs.fromBundle(getArguments());
        Role role = args.getRole();
        String communityId = args.getCommunityId();
        String userId = args.getUserId();

        if (role == Role.ADMIN) communityService.deleteAdmin(communityId, userId, onResult::accept);
        else communityService.deleteParticipant(communityId, userId, onResult::accept);
    }

}

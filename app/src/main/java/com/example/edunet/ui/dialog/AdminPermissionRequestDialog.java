package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminPermissionRequestDialog extends DialogFragment {

    @Inject
    CommunityService communityService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String cid = AdminPermissionRequestDialogArgs.fromBundle(getArguments()).getCommunityId();

        return new AlertDialog.Builder(requireContext())
                .setMessage(R.string.request_admin_permissions_dialog)
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        communityService.requestAdminPermissions(cid, (exception) -> {
                            if (exception != null)
                                Toast.makeText(requireContext().getApplicationContext(), exception.getId(), Toast.LENGTH_SHORT).show();
                        }))

                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .create();
    }

}

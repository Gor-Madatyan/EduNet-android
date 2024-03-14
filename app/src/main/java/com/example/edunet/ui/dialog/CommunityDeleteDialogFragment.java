package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.task.community.CommunityTaskManager;
import com.example.edunet.data.service.util.work.WorkUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommunityDeleteDialogFragment extends DialogFragment {
    @Inject
    CommunityTaskManager communityTaskManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String communityId = CommunityDeleteDialogFragmentArgs.fromBundle(getArguments()).getCommunityId();

        return new AlertDialog.Builder(requireContext())
                .setMessage(R.string.community_delete_dialog)
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                    WorkUtils.observe(requireContext().getApplicationContext(),
                            communityTaskManager.startCommunityDeleteTask(communityId),
                            R.string.error_cant_delete_community
                            )
                )
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .create();
    }

}

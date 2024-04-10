package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.fragment.NavHostFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.ui.screen.adminpanel.members.MembersFragment;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GraduateDialog extends DialogFragment {
    private static final String TAG = GraduateDialog.class.getSimpleName();
    @Inject
    CommunityService communityService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        SavedStateHandle previousSavedStateHandle = Objects.requireNonNull(NavHostFragment.findNavController(this).getPreviousBackStackEntry()).getSavedStateHandle();
        var args = GraduateDialogArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        String[] participants = args.getParticipants();

        return new AlertDialog.Builder(requireContext())
                .setMessage(R.string.dialog_add_graduated)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            previousSavedStateHandle.set(MembersFragment.ARE_PARTICIPANTS_GRADUATED_KEY, true);
                            communityService.graduateParticipants(communityId, participants,
                                    e -> {
                                        if (e != null) {
                                            Log.w(TAG, e);
                                            Toast.makeText(requireContext().getApplicationContext(), R.string.error_cant_graduate_participants, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }

                )
                .setNegativeButton(android.R.string.no, (dialog, which) -> previousSavedStateHandle.set(MembersFragment.ARE_PARTICIPANTS_GRADUATED_KEY, false))
                .create();
    }
}

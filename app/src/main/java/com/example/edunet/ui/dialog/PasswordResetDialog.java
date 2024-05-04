package com.example.edunet.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PasswordResetDialog extends DialogFragment {
    @Inject
    AccountService accountService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String email = PasswordResetDialogArgs.fromBundle(requireArguments()).getEmail();

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_password_title)
                .setMessage(R.string.reset_password_dialog)
                .setPositiveButton(R.string.send_email, (dialog, which) ->
                        accountService.sendPasswordResetEmail(email, e -> {
                            if (e != null)
                                Toast.makeText(requireContext(), e.getId(), Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {

                }).create();
    }
}

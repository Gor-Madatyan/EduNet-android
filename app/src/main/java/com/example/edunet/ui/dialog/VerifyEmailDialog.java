package com.example.edunet.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.credentials.CredentialManager;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.ui.util.CredentialUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VerifyEmailDialog extends DialogFragment {
    private NavController navController;
    @Inject
    AccountService accountService;
    @Inject
    CredentialManager credentialManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        navController = NavHostFragment.findNavController(this);
        return new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.verify_email_title)
                .setMessage(R.string.verify_email)
                .setPositiveButton(R.string.send_verify_message, (dialog, which) ->
                        accountService.sendEmailVerification(e -> {
                            if (e != null)
                                Toast.makeText(requireContext(), e.getId(), Toast.LENGTH_SHORT).show();
                            else{
                                accountService.signOut();
                                CredentialUtils.clearCredentials(credentialManager);
                                navController.navigate(VerifyEmailDialogDirections.actionGlobalSignInFragment());
                            }
                        }))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                }).create();
    }
}

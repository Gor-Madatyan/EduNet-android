package com.example.edunet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserChangeRequest;
import com.example.edunet.ui.util.FireBaseAuthUiUtils;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
public class StartUpActivity extends AppCompatActivity {

    private final static String TAG = StartUpActivity.class.getSimpleName();
    private final static AccountService accountService = AccountService.IMPL;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            User user = accountService.getCurrentUser();
            assert user != null : AccountService.ErrorMessages.CURRENT_USER_IS_NULL;

            if (user.isAnonymous()) {
                accountService.updateCurrentUser(new UserChangeRequest().setName("Anonymous"), e -> {
                            if (e != null) {
                                Log.e(TAG, "User name update failed", e);
                            }
                        }
                );
            }
            startActivity(new Intent(this, MainActivity.class));
        } else {
            IdpResponse response = result.getIdpResponse();
            if (response != null) {
                FirebaseUiException error = response.getError();
                Log.e(TAG, "Sign in/up Error occurred", error);
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (accountService.getCurrentUser() == null) {
            signInLauncher.launch(FireBaseAuthUiUtils.getIntent());
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
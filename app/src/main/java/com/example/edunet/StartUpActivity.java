package com.example.edunet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.ui.util.FireBaseAuthUiUtils;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StartUpActivity extends AppCompatActivity {

    private final static String TAG = StartUpActivity.class.getSimpleName();
    @Inject
    AccountService accountService;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            User user = accountService.getCurrentUser();
            assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;
            assert response != null;

            startActivity(new Intent(this, MainActivity.class));
        } else {
            if (response != null) {
                FirebaseUiException error = response.getError();
                assert error != null;
                Log.e(TAG, error.toString());
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
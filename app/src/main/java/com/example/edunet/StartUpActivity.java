package com.example.edunet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edunet.data.service.api.AccountService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
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

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            User user = accountService.getCurrentUser();
            assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

            if (user.isAnonymous()) {
                accountService.updateCurrentUser(new UserUpdateRequest().setName("Anonymous"), e -> {
                            if (e != null) {
                                Log.e(TAG, e.toString());
                            }
                        }
                );
            }
            startActivity(new Intent(this, MainActivity.class));
        } else {
            IdpResponse response = result.getIdpResponse();
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
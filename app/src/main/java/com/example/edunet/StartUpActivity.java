package com.example.edunet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.model.UserChangeRequest;
import com.example.edunet.utils.FireBaseAuthUiUtils;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class StartUpActivity extends AppCompatActivity {

    private final static String TAG = StartUpActivity.class.getSimpleName();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            var user = auth.getCurrentUser();
            assert user != null : "sign in succeeded, the current user must be non-null";

            if (user.isAnonymous()) {
                AccountService.IMPL.updateCurrentUser(new UserChangeRequest().setNewName("Anonymous"), e -> {
                            Log.e(TAG, "User name update failed");
                            String message = e.getMessage();

                            if (message != null)
                                Log.e(TAG,message);
                        }
                );
            }
            startActivity(new Intent(this, MainActivity.class));
        } else {
            IdpResponse response = result.getIdpResponse();
            if (response != null) {
                var error = response.getError();
                assert error != null : "error cant be null because sign in failed";
                Log.e(TAG,Objects.requireNonNullElse(error.getMessage(), "Program Error occurred"));
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signInLauncher.launch(FireBaseAuthUiUtils.getIntent());
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
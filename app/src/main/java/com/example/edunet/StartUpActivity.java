package com.example.edunet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edunet.utils.AuthUtils;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class StartUpActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            IdpResponse response = result.getIdpResponse();
            if (response != null) {
                //if authentication failed and response is not null, response.getError().getMessage() will not produce NullPointerException
                Toast.makeText(this, Objects.requireNonNullElse(response.getError().getMessage(),"Program Error occurred"), Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signInLauncher.launch(AuthUtils.getIntent());
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
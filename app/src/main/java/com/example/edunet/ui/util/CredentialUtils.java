package com.example.edunet.ui.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.credentials.CreatePasswordRequest;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetPasswordOption;

import com.example.edunet.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;

import java.util.Arrays;

public final class CredentialUtils {
    private CredentialUtils() {
    }

    @NonNull
    public static CreatePasswordRequest createPasswordRequest(String email, String password) {
        return new CreatePasswordRequest(email, password);
    }

    @NonNull
    public static GetCredentialRequest getCredentialRequest(Context context) {
        return new GetCredentialRequest.Builder()
                .setPreferImmediatelyAvailableCredentials(true)
                .setCredentialOptions(
                        Arrays.asList(
                                new GetPasswordOption(),
                                new GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(context.getString(R.string.default_web_client_id)).build()

                        )
                ).build();
    }

    @NonNull
    public static GetCredentialRequest getGoogleButtonFlowRequest(Context context) {
        return new GetCredentialRequest.Builder()
                .addCredentialOption(
                        new GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
                ).build();
    }
}

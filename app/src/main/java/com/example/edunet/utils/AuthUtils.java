package com.example.edunet.utils;

import android.content.Intent;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;

public final class AuthUtils {
    private AuthUtils(){}

    public static Intent getIntent(){

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build()
                );

        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
    }
}

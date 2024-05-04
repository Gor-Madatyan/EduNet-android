package com.example.edunet.ui.screen.auth.signin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;
import com.example.edunet.data.service.exception.UserFriendlyException;
import com.example.edunet.databinding.FragmentAuthBinding;
import com.example.edunet.ui.util.CredentialUtils;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignInFragment extends Fragment {
    private static final String TAG = SignInFragment.class.getSimpleName();
    private NavController navController;
    private FragmentAuthBinding binding;
    private SignInViewModel viewModel;
    @Inject
    CredentialManager credentialManager;

    private void onCredentialRetrieved(GetCredentialResponse getCredentialResponse) {
        binding.progressIndicator.setVisibility(View.VISIBLE);
        Consumer<UserFriendlyException> redirectOnSuccess = e -> {
            if (e == null)
                navController.navigate(SignInFragmentDirections.actionSignInFragmentToNavigationChats());
            else binding.progressIndicator.hide();
        };

        Credential credential = getCredentialResponse.getCredential();
        if (credential instanceof PasswordCredential passwordCredential) {
            viewModel.signInWithEmailAndPassword(passwordCredential.getId(), passwordCredential.getPassword(), redirectOnSuccess);
        } else if (credential instanceof CustomCredential customCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                viewModel.signInWithGoogleId(googleIdTokenCredential.getIdToken(), redirectOnSuccess);
            } else
                Log.e(TAG, "Unexpected type of credential");

        } else
            Log.e(TAG, "Unexpected type of credential");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignInViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return (binding = FragmentAuthBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding.redirectSignUp.setVisibility(View.VISIBLE);
        binding.googleButtonContainer.setVisibility(View.VISIBLE);
        binding.sendPasswordResetEmail.setVisibility(View.VISIBLE);
        CredentialManagerCallback<GetCredentialResponse, GetCredentialException> callback = new CredentialManagerCallback<>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                onCredentialRetrieved(getCredentialResponse);
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                Log.e(TAG,"can't get credential", e);
            }
        };

        binding.submit.setOnClickListener(v -> {
                    String email = binding.editEmail.getText().toString().trim();
                    String password = binding.editPassword.getText().toString().trim();
                    if (email.isEmpty() || password.isEmpty())
                        return;

                    binding.progressIndicator.setVisibility(View.VISIBLE);
                    binding.submit.setEnabled(false);
                    viewModel.signInWithEmailAndPassword(email, password, e -> {
                        if (e != null) {
                            binding.submit.setEnabled(true);
                            binding.progressIndicator.hide();
                            binding.error.setText(e.getId());
                            return;
                        }

                        navController.navigate(SignInFragmentDirections.actionSignInFragmentToNavigationChats());
                    });
                }
        );

        binding.redirectSignUp.setOnClickListener(v ->
                navController.navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment()));

        binding.sendPasswordResetEmail.setOnClickListener(v-> {
            String email = binding.editEmail.getText().toString().trim();
            if(!viewModel.validateEmail(email)){
                binding.editEmail.setError(getString(R.string.invalid_email));
                return;
            }
            navController.navigate(SignInFragmentDirections.actionSignInFragmentToPasswordResetDialog(email));
        });

        binding.googleButton.setOnClickListener(v ->
                credentialManager.getCredentialAsync(
                        requireActivity(),
                        CredentialUtils.getGoogleButtonFlowRequest(requireActivity()),
                        null,
                        Runnable::run,
                        callback
                )
        );

        credentialManager.getCredentialAsync(
                requireActivity(),
                CredentialUtils.getCredentialRequest(requireActivity()),
                null,
                Runnable::run,
                callback
        );

    }
}
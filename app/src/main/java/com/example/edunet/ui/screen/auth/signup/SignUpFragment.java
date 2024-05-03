package com.example.edunet.ui.screen.auth.signup;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.data.service.model.EmailCredential;
import com.example.edunet.databinding.FragmentAuthBinding;
import com.example.edunet.ui.util.CredentialUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {
    private static final String TAG = SignUpFragment.class.getSimpleName();
    private NavController navController;
    private FragmentAuthBinding binding;
    private SignUpViewModel viewModel;
    @Inject
    CredentialManager credentialManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
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
        binding.editName.setVisibility(View.VISIBLE);
        binding.confirmPassword.setVisibility(View.VISIBLE);

        binding.submit.setOnClickListener(v -> {
            Context context = requireActivity();
            String email = binding.editEmail.getText().toString().trim();
            String name = binding.editName.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();
            String confirmPassword = binding.confirmPassword.getText().toString().trim();

            if( email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty())
                return;

            if(!password.equals(confirmPassword)){
                binding.confirmPassword.setError("Passwords does not match");
                return;
            }

            EmailCredential EmailCredential = new EmailCredential(name, email, password);

            binding.progressIndicator.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);
            viewModel.signUpWithEmailAddress(EmailCredential, e->{
                if(e != null){
                    binding.submit.setEnabled(true);
                    binding.progressIndicator.hide();
                    binding.error.setText(e.getId());
                    return;
                }

                credentialManager.createCredentialAsync(
                        context,
                        CredentialUtils.createPasswordRequest(email, password),
                        null,
                        Runnable::run,
                        new CredentialManagerCallback<>() {

                            @Override
                            public void onResult(CreateCredentialResponse createCredentialResponse) {
                                Log.i(TAG, "password created");
                            }

                            @Override
                            public void onError(@NonNull CreateCredentialException e) {
                                Log.e(TAG, "password creation failed", e);
                            }
                        }
                );
                navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToNavigationProfile());
            });
        });
    }
}
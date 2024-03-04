package com.example.edunet.ui.screen.profile.update;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.bumptech.glide.Glide;
import com.example.edunet.R;
import com.example.edunet.databinding.FragmentProfileUpdateBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileUpdateFragment extends Fragment {
    @Inject
    NavController navController;
    private ProfileUpdateViewModel viewModel;
    private FragmentProfileUpdateBinding binding;

    private final ActivityResultLauncher<String[]> mediaPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
            uri -> viewModel.setTemporaryImage(uri)
    );


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileUpdateViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.editName.setText(viewModel.getInitialName());
        binding.editBio.setText(viewModel.getInitialBio());

        binding.submit.setOnClickListener(v -> {
            String newName = binding.editName.getText().toString();
            String newBio = binding.editBio.getText().toString();
            Uri photo = viewModel.userPhoto.getValue();

            if (photo != null && !photo.equals(viewModel.getInitialAvatar()))
                requireActivity().getContentResolver().takePersistableUriPermission(photo,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );


            viewModel.updateProfile(newName, newBio, requireContext().getApplicationContext());
        });

        binding.avatar.setOnClickListener(v ->
                mediaPickerLauncher.launch(new String[]{"image/*"})
        );

        viewModel.userPhoto.observe(getViewLifecycleOwner(),
                photo -> Glide.with(this)
                        .load(photo)
                        .placeholder(R.drawable.ic_default_user)
                        .circleCrop()
                        .into(binding.avatar));

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null)
                binding.error.setText(getString(error.messageId()));
            else navController.navigateUp();
        });
    }

}
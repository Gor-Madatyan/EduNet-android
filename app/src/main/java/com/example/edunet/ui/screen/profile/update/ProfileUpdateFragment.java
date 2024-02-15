package com.example.edunet.ui.screen.profile.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.edunet.R;
import com.example.edunet.databinding.FragmentProfileUpdateBinding;

public class ProfileUpdateFragment extends Fragment {
    private NavController navController;
    private ProfileUpdateViewModel viewModel;
    private FragmentProfileUpdateBinding binding;

    private final ActivityResultLauncher<PickVisualMediaRequest> mediaPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
            uri -> viewModel.setTemporaryImage(uri)
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.editName.setText(viewModel.getInitialName());

        binding.submit.setOnClickListener(v -> {
            String newName = binding.editName.getText().toString();

            viewModel.updateProfile(newName, requireContext().getContentResolver());
        });

        binding.avatar.setOnClickListener(v ->
                mediaPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        viewModel.userPhoto.observe(getViewLifecycleOwner(),
                uri -> Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_default_user)
                        .circleCrop()
                        .into(binding.avatar));
        
        viewModel.result.observe(getViewLifecycleOwner(), result -> {
            if (result.haveError())
                binding.error.setText(result.message());
            else navController.navigateUp();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileUpdateViewModel.class);
    }

}
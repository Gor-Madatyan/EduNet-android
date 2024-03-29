package com.example.edunet.ui.screen.addcommunity;

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
import androidx.navigation.Navigation;

import com.example.edunet.databinding.FragmentModifyCommunityBinding;
import com.example.edunet.ui.util.ImageLoadingUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddCommunityFragment extends Fragment {

    private AddCommunityViewModel viewModel;
    private FragmentModifyCommunityBinding binding;
    private NavController navController;

    private final ActivityResultLauncher<String[]> mediaPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
            uri -> viewModel.setAvatar(uri)
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddCommunityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentModifyCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String ancestor = AddCommunityFragmentArgs.fromBundle(getArguments()).getAncestor();
        navController = Navigation.findNavController(view);

        binding.avatar.setOnClickListener(v -> mediaPickerLauncher.launch(new String[]{"image/*"}));

        viewModel.avatar.observe(getViewLifecycleOwner(), uri ->
                ImageLoadingUtils.loadCommunityAvatar(this, uri, binding.avatar)
        );

        viewModel.error.observe(getViewLifecycleOwner(), e -> {
            if (e == null) {
                navController.navigateUp();
                return;
            }

            binding.error.setText(e.messageId());
        });

        binding.submit.setOnClickListener(v -> {
            String name = binding.editName.getText().toString();
            String description = binding.editDescription.getText().toString();
            Uri photo = viewModel.avatar.getValue();

           if (photo != null)
                requireActivity().getContentResolver().takePersistableUriPermission(photo,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

            viewModel.createCommunity(name, description, ancestor, requireContext().getApplicationContext());
        });

    }
}
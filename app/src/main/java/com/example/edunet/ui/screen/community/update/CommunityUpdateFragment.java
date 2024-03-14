package com.example.edunet.ui.screen.community.update;

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

import com.example.edunet.data.service.model.Community;
import com.example.edunet.databinding.FragmentModifyCommunityBinding;
import com.example.edunet.ui.util.ImageLoadingUtils;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommunityUpdateFragment extends Fragment {
    private NavController navController;
    private FragmentModifyCommunityBinding binding;
    private CommunityUpdateViewModel viewModel;
    private final ActivityResultLauncher<String[]> mediaPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
            uri -> viewModel.setAvatar(uri)
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentModifyCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        CommunityUpdateFragmentArgs args = CommunityUpdateFragmentArgs.fromBundle(getArguments());
        Community community = args.getCommunity();
        String communityId = args.getCommunityId();

        binding.editName.setText(community.getName());
        binding.editDescription.setText(community.getDescription());
        viewModel.setCommunity(communityId, community);

        viewModel.avatar.observe(getViewLifecycleOwner(), uri ->
                ImageLoadingUtils.loadCommunityAvatar(
                        this,
                        uri,
                        binding.avatar));

        binding.avatar.setOnClickListener(v ->
                mediaPickerLauncher.launch(new String[]{"image/*"})
        );

        viewModel.error.observe(getViewLifecycleOwner(),
                e -> {
                    if (e == null)
                        navController.navigateUp();
                    else binding.error.setText(e.id());
                });

        binding.submit.setOnClickListener(
                v -> {
                    String name = binding.editName.getText().toString();
                    String description = binding.editDescription.getText().toString();
                    Uri photo = viewModel.avatar.getValue();

                    if(photo != null && !Objects.equals(viewModel.getInitialAvatar(), viewModel.avatar.getValue()))
                        requireActivity().getContentResolver().takePersistableUriPermission(photo,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    viewModel.updateCommunity(requireContext().getApplicationContext(),name,description);
                }
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunityUpdateViewModel.class);
    }
}

package com.example.edunet.ui.screen.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.edunet.databinding.FragmentSearchBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {

    private FragmentSearchBinding binding;
    private NotificationsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String communityId = NotificationsFragmentArgs.fromBundle(requireArguments()).getCommunityId();

        binding.result.setAdapter(viewModel.getNotificationsAdapter(communityId));
    }
}
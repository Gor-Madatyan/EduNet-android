package com.example.edunet.ui.screen.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Entity;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.util.adapter.impl.EntityAdapter;

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatsFragment extends Fragment {
    private FragmentSearchBinding binding;
    private ChatsViewModel viewModel;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel.setListener(getViewLifecycleOwner());
        viewModel.dataset.observe(getViewLifecycleOwner(), communities ->
                binding.result.setAdapter(new EntityAdapter<>(Arrays.asList(communities), R.layout.name_avatar_element, data ->
                        data.getView().setOnClickListener(v1 -> {
                            Entity community = data.getEntity();
                            navController.navigate(ChatsFragmentDirections.actionNavigationChatsToChatFragment(community.getName(), community.getAvatar(), community.getId()));
                        }))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
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
import com.example.edunet.common.util.UriUtils;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.adapter.EntityAdapter;

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
                binding.result.setAdapter(new EntityAdapter<>(Arrays.asList(communities), R.layout.name_avatar_element, (v, data) ->
                        v.setOnClickListener(v1 -> navController.navigate(ChatsFragmentDirections.actionNavigationChatsToChatFragment(data.getTitle(), UriUtils.safeToString(data.getAvatar())))))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
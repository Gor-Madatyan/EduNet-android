package com.example.edunet.ui.screen.graduations;

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
public class GraduationsFragment extends Fragment {
    private FragmentSearchBinding binding;
    private GraduationsViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GraduationsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (binding = FragmentSearchBinding.inflate(inflater,container,false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String communityId = GraduationsFragmentArgs.fromBundle(getArguments()).getCommunityId();
        viewModel.setCommunity(communityId);

        viewModel.adapterLiveData.observe(getViewLifecycleOwner(),adapter->
            binding.result.setAdapter(adapter)
        );
    }
}
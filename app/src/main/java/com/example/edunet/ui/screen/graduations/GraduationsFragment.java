package com.example.edunet.ui.screen.graduations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.edunet.R;
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
        return (binding = FragmentSearchBinding.inflate(inflater, container, false)).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String communityId = GraduationsFragmentArgs.fromBundle(getArguments()).getCommunityId();
        viewModel.setCommunity(communityId);

        viewModel.viewerClassAdapterLiveData.observe(getViewLifecycleOwner(), ignore ->
                requireActivity().addMenuProvider(new MenuProvider() {
                    @Override
                    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                        menuInflater.inflate(R.menu.graduations_toolbar_menu, menu);
                    }

                    @Override
                    public void onPrepareMenu(@NonNull Menu menu) {
                        MenuProvider.super.onPrepareMenu(menu);
                        MenuItem actionShowViewerClass = menu.findItem(R.id.actionShowViewerClass);

                        viewModel.isShowingViewerClassLiveData.observe(getViewLifecycleOwner(), mode ->
                                actionShowViewerClass.setIcon(mode ? R.drawable.ic_group_off_40dp : R.drawable.ic_group_40dp)
                        );
                    }

                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                        viewModel.toggleShowingViewerClass();
                        return true;
                    }
                }, getViewLifecycleOwner()));

        viewModel.graduationsAdapterLiveData.observe(getViewLifecycleOwner(), adapter ->
                binding.result.setAdapter(adapter)
        );

        viewModel.isShowingViewerClassLiveData.observe(getViewLifecycleOwner(),
                mode ->
                        binding.result.setAdapter(mode ? viewModel.viewerClassAdapterLiveData.getValue() : viewModel.graduationsAdapterLiveData.getValue())
        );
    }
}
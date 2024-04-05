package com.example.edunet.ui.screen.adminpanel.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Entity;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.adapter.EntityAdapter;
import com.example.edunet.ui.adapter.LazyEntityAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestsFragment extends Fragment {
    public static final String IS_REQUEST_MANAGED_KEY = "IS_REQUEST_MANAGED";
    private FragmentSearchBinding binding;
    private RequestsViewModel viewModel;
    private NavController navController;
    private SavedStateHandle currentSavedStateHandle;
    private EntityAdapter<User> entityAdapter;
    private int lastManagedItemPosition;

    private void listenRequestManagement() {
        currentSavedStateHandle.<Boolean>getLiveData(IS_REQUEST_MANAGED_KEY).observe(getViewLifecycleOwner(),
                isManaged -> {
                    if (isManaged)
                        entityAdapter.deleteItem(lastManagedItemPosition);
                }
        );
    }

    private void managePermissions(int position, String communityId, Entity entity, boolean accept, Role role) {
        lastManagedItemPosition = position;
        navController.navigate(RequestsFragmentDirections.actionRequestsFragmentToManagePermissionsDialog(communityId, entity.getId(), accept, role));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RequestsViewModel.class);
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
        navController = Navigation.findNavController(view);
        currentSavedStateHandle = navController.getBackStackEntry(R.id.requestsFragment).getSavedStateHandle();
        var args = RequestsFragmentArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        Role role = args.getRole();
        viewModel.setCommunity(communityId, role);
        listenRequestManagement();

        viewModel.paginator.observe(getViewLifecycleOwner(), paginator -> {
            entityAdapter = new LazyEntityAdapter<>(paginator, R.layout.manageable_name_avatar_element, (item, data) -> {
                item.findViewById(R.id.add).setOnClickListener(
                        v -> managePermissions(data.getPosition(), communityId, data.getEntity(), true, role)
                );
                item.findViewById(R.id.remove).setOnClickListener(
                        v -> managePermissions(data.getPosition(), communityId, data.getEntity(), false, role)
                );
            });

            binding.result.setAdapter(entityAdapter);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
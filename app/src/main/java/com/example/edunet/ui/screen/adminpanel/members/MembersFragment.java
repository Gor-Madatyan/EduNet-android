package com.example.edunet.ui.screen.adminpanel.members;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.adapter.EntityAdapter;
import com.example.edunet.ui.adapter.LazyEntityAdapter;
import com.example.edunet.ui.itemtouchhelper.ItemTouchHelpers;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MembersFragment extends Fragment {
    public static final String IS_ITEM_DELETED_KEY = "IS_ITEM_DELETED";

    private FragmentSearchBinding binding;
    private MembersViewModel viewModel;
    private NavController navController;
    private SavedStateHandle currentSavedStateHandle;
    private EntityAdapter<User> entityAdapter;
    private int lastManagedItemPosition;

    private void listenItemDeletions(){
        currentSavedStateHandle.<Boolean>getLiveData(IS_ITEM_DELETED_KEY).observe(getViewLifecycleOwner(),
                isDeleted -> {
                    if (isDeleted)
                        entityAdapter.deleteItem(lastManagedItemPosition);
                }
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
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
        currentSavedStateHandle = navController.getBackStackEntry(R.id.membersFragment).getSavedStateHandle();
        var args = MembersFragmentArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        Role role = args.getRole();
        viewModel.setCommunity(communityId, role);
        listenItemDeletions();

        viewModel.paginator.observe(getViewLifecycleOwner(), paginator -> {
            entityAdapter = new LazyEntityAdapter<>(paginator, R.layout.name_avatar_element, (item, data) -> {
            });
            binding.result.setAdapter(entityAdapter);
        });
        Context context = requireContext();

        ItemTouchHelpers.getRightSwipableItemTouchHelper(
                position -> {
                    lastManagedItemPosition = position;
                    entityAdapter.notifyItemChanged(position);
                    navController.navigate(MembersFragmentDirections.actionMembersFragmentToDeleteMemberDialog(
                            role,
                            communityId,
                            entityAdapter.getItem(position).id()
                    ));
                },
                context.getColor(R.color.error),
                Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.ic_delete_40dp))).attachToRecyclerView(binding.result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

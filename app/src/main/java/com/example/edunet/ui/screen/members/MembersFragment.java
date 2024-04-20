package com.example.edunet.ui.screen.members;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.util.adapter.ListAdapter;
import com.example.edunet.ui.util.adapter.util.SelectableAdapterUtils;
import com.example.edunet.ui.util.itemtouchhelper.ItemTouchHelpers;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MembersFragment extends Fragment {
    public static final String IS_ITEM_DELETED_KEY = "IS_ITEM_DELETED";
    public static final String ARE_PARTICIPANTS_GRADUATED_KEY = "ARE_PARTICIPANTS_GRADUATED";

    private FragmentSearchBinding binding;
    private MembersViewModel viewModel;
    private NavController navController;
    private SavedStateHandle currentSavedStateHandle;
    private ListAdapter<?, User> entityAdapter;


    private void listenItemDeletions() {
        currentSavedStateHandle.<Boolean>getLiveData(IS_ITEM_DELETED_KEY).observe(getViewLifecycleOwner(),
                isDeleted -> {
                    if (isDeleted)
                        viewModel.removeLastManagedItem();
                }
        );
    }

    private void listenParticipantGraduations() {
        currentSavedStateHandle.<Boolean>getLiveData(ARE_PARTICIPANTS_GRADUATED_KEY).observe(getViewLifecycleOwner(),
                areGraduated -> {
                    if (areGraduated)
                        viewModel.removeSelections();
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
        viewModel.setCommunity(communityId, role, getViewLifecycleOwner());
        listenItemDeletions();
        if (role == Role.PARTICIPANT) listenParticipantGraduations();

        viewModel.entityAdapterLiveData.observe(
                getViewLifecycleOwner(), adapter -> {
                    binding.result.setAdapter(adapter);
                    entityAdapter = adapter;
                });

        if (role == Role.PARTICIPANT)
            requireActivity().addMenuProvider(
                    new MenuProvider() {
                        @Override
                        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                            menuInflater.inflate(R.menu.members_toolbar_menu, menu);
                        }

                        @Override
                        public void onPrepareMenu(@NonNull Menu menu) {
                            MenuItem selectionMode = menu.findItem(R.id.actionSelectionMode);
                            MenuItem graduate = menu.findItem(R.id.actionGraduate);
                            MenuItem selectAll = menu.findItem(R.id.selectAll);

                            viewModel.selectionModeLiveData.observe(getViewLifecycleOwner(),
                                    mode ->
                                            selectionMode.setIcon(!mode ? R.drawable.ic_checklist_rtl_40dp : R.drawable.ic_crossed_out_checklist_rtl_40dp)

                            );

                            viewModel.selectionStateLiveData.observe(getViewLifecycleOwner(),
                                    state -> {
                                        int size = state.size();
                                        boolean mode = state.selectionMode();
                                        boolean isNode = state.isNode();
                                        Role role = state.viewerRole();
                                        boolean isRoleHigh = role == Role.ADMIN || role == Role.OWNER;

                                        selectionMode.setVisible(isRoleHigh && isNode);
                                        graduate.setVisible(isRoleHigh && isNode && mode && size > 0);
                                        selectAll.setVisible(isRoleHigh && isNode && mode && size < entityAdapter.getDataset().size());
                                    }
                            );
                        }

                        @Override
                        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.actionSelectionMode) {
                                viewModel.toggleSelectionMode();
                                return true;
                            } else if (menuItem.getItemId() == R.id.selectAll) {
                                SelectableAdapterUtils.selectAll(viewModel.selector, entityAdapter);
                                return true;
                            } else if (menuItem.getItemId() == R.id.actionGraduate) {
                                navController.navigate(MembersFragmentDirections.actionMembersFragmentToGraduateDialog(
                                                viewModel.selector.getSelections().stream().map(position -> entityAdapter.getDataset().get(position).getId()).toArray(String[]::new),
                                                communityId
                                        )
                                );
                                return true;
                            }
                            return false;
                        }

                    },

                    getViewLifecycleOwner());

        viewModel.selectionModeLiveData.observe(getViewLifecycleOwner(),
                mode -> {
                    if (!mode && viewModel.selector.size() > 0)
                        SelectableAdapterUtils.reset(viewModel.selector, entityAdapter);
                }
        );
        viewModel.isNodeLiveData.observe(getViewLifecycleOwner(),
                isNode -> {
                    if(!isNode && viewModel.getSelectionMode()) viewModel.toggleSelectionMode();
                }
        );

        Context context = requireContext();

        ItemTouchHelpers.getRightSwipableItemTouchHelper(
                position -> {
                    viewModel.setLastManagedItemPosition(position);
                    entityAdapter.notifyItemChanged(position);
                    navController.navigate(MembersFragmentDirections.actionMembersFragmentToDeleteMemberDialog(
                            role,
                            communityId,
                            entityAdapter.getDataset().get(position).getId()
                    ));
                },
                () -> {
                    Role viewerRole = viewModel.getViewerRole();
                    return !viewModel.getSelectionMode() && (viewerRole == Role.ADMIN || viewerRole == Role.OWNER);
                },
                context.getColor(R.color.error),
                Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.ic_delete_40dp))).attachToRecyclerView(binding.result);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        currentSavedStateHandle.remove(IS_ITEM_DELETED_KEY);
        currentSavedStateHandle.remove(ARE_PARTICIPANTS_GRADUATED_KEY);
    }
}

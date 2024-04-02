package com.example.edunet.ui.screen.adminpanel.members;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.edunet.R;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.databinding.FragmentSearchBinding;
import com.example.edunet.ui.adapter.EntityAdapter;
import com.example.edunet.ui.adapter.LazyEntityAdapter;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MembersFragment extends Fragment {
    private static final String TAG = MembersFragment.class.getSimpleName();
    private FragmentSearchBinding binding;
    private MembersViewModel viewModel;

    @SuppressWarnings("unchecked")
    private void deleteMember(int position) {
        ((EntityAdapter<User>) Objects.requireNonNull(binding.result.getAdapter())).deleteItem(position);
    }

    private void processOperation(Exception e) {
        if (e != null) {
            String message = "cant complete operation";
            Log.w(TAG, e);
            Toast.makeText(requireContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
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
        var args = MembersFragmentArgs.fromBundle(getArguments());
        String communityId = args.getCommunityId();
        Role role = args.getRole();

        viewModel.setCommunity(communityId, role);

        viewModel.paginator.observe(getViewLifecycleOwner(), paginator ->
                binding.result.setAdapter(new LazyEntityAdapter<>(paginator, R.layout.manageable_name_avatar_element, (item, data) ->
                        item.findViewById(R.id.remove).setOnClickListener(
                                v -> {
                                    viewModel.delete(data.getEntity().getId(), this::processOperation);
                                    deleteMember(data.getPosition());
                                }
                        ))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

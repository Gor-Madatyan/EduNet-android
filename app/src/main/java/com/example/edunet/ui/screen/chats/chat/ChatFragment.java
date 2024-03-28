package com.example.edunet.ui.screen.chats.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.example.edunet.R;
import com.example.edunet.common.util.UriUtils;
import com.example.edunet.databinding.FragmentChatBinding;
import com.example.edunet.ui.util.ImageLoadingUtils;

import java.util.Objects;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String avatar = ChatFragmentArgs.fromBundle(getArguments()).getAvatar();

        binding.message.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    binding.send.setVisibility(View.VISIBLE);
                else binding.send.setVisibility(View.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.chat_toolbar_menu, menu);
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
                ImageView avatarView = Objects.requireNonNull(menu.getItem(0).getActionView()).findViewById(R.id.avatar);
                assert avatarView != null;

                ImageLoadingUtils.loadCommunityAvatar(ChatFragment.this, UriUtils.safeParse(avatar), avatarView);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        requireActivity().findViewById(R.id.bottom_nav_view).setVisibility(View.VISIBLE);
    }
}

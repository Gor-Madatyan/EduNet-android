package com.example.edunet.ui.util;

import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.edunet.R;

public final class GlideUtils {
    private GlideUtils() {
    }

    private static void loadAvatar(@NonNull Fragment fragment,
                                   @Nullable Uri uri,
                                   @DrawableRes int placeholder,
                                   @NonNull ImageView view) {
        Glide.with(fragment)
                .load(uri)
                .circleCrop()
                .placeholder(placeholder)
                .into(view);
    }

    public static void loadUserAvatar(
            @NonNull Fragment fragment,
            @Nullable Uri uri,
            @NonNull ImageView view
    ) {
        loadAvatar(fragment, uri, R.drawable.ic_default_user, view);
    }

    public static void loadCommunityAvatar(
            @NonNull Fragment fragment,
            @Nullable Uri uri,
            @NonNull ImageView view
    ) {
        loadAvatar(fragment, uri, R.drawable.ic_default_group, view);
    }
}

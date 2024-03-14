package com.example.edunet.ui.util;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.edunet.R;

public final class ImageLoadingUtils {
    private ImageLoadingUtils() {
    }

    private static void loadAvatar(@NonNull RequestManager requestManager,
                                   @Nullable Uri uri,
                                   @DrawableRes int placeholder,
                                   @NonNull ImageView into) {
        requestManager.load(uri)
                .circleCrop()
                .placeholder(placeholder)
                .into(into);
    }

    public static void loadUserAvatar(
            @NonNull Fragment fragment,
            @Nullable Uri uri,
            @NonNull ImageView into
    ) {
        loadAvatar(Glide.with(fragment), uri, R.drawable.ic_default_user, into);
    }

    public static void loadCommunityAvatar(
            @NonNull Fragment fragment,
            @Nullable Uri uri,
            @NonNull ImageView into
    ) {
        loadAvatar(Glide.with(fragment), uri, R.drawable.ic_default_group, into);
    }

    public static void loadCommunityAvatar(
            @NonNull View view,
            @Nullable Uri uri,
            @NonNull ImageView into
    ) {
        loadAvatar(Glide.with(view), uri, R.drawable.ic_default_group, into);
    }
}

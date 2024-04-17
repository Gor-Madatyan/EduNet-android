package com.example.edunet.util;

import android.net.Uri;

import androidx.annotation.Nullable;

public final class UriUtils {
    private UriUtils(){}

    @Nullable
    public static Uri safeParse(@Nullable String uri){
        return uri == null ? null : Uri.parse(uri);
    }

    @Nullable
    public static String safeToString(@Nullable Uri uri){
        return uri == null ? null : uri.toString();
    }
}

package com.example.edunet.data.service.util.firebase.storage;

import android.net.Uri;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

public final class UriValidator {
    private UriValidator(){}

    public static boolean isValidUploadUri(@NonNull Uri uri) {
        String uriString = uri.toString();

        return URLUtil.isValidUrl(uriString) &&
                (URLUtil.isFileUrl(uriString) ||
                        URLUtil.isContentUrl(uriString));
    }

}

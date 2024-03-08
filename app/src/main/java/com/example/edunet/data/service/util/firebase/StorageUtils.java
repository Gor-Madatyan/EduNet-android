package com.example.edunet.data.service.util.firebase;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.google.firebase.storage.StorageReference;

public final class StorageUtils {

    private StorageUtils() {
    }

    public static boolean validatePhoto(@Nullable Uri photo) {
        if (photo == null) return false;

        String scheme = photo.getScheme();
        if (scheme == null) return false;

        return scheme.equals(ContentResolver.SCHEME_CONTENT) || scheme.equals(ContentResolver.SCHEME_FILE);
    }

    public static void savePhoto(@NonNull StorageReference storageRef,
                                 @NonNull Uri photo,
                                 @NonNull Consumer<Uri> onSuccess,
                                 @NonNull Consumer<Exception> onFailure) {
        if (!validatePhoto(photo)) {
            onFailure.accept(new IllegalArgumentException("Provided invalid photo"));
            return;
        }

        storageRef
                .putFile(photo)
                .addOnSuccessListener(
                        taskSnapshot -> storageRef.getDownloadUrl()
                                .addOnSuccessListener(onSuccess::accept)
                                .addOnFailureListener(onFailure::accept)
                )
                .addOnFailureListener(onFailure::accept);
    }

}

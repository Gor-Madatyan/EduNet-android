package com.example.edunet.data.service.impl.storage;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.example.edunet.R;
import com.example.edunet.data.service.api.AccountService;
import com.example.edunet.data.service.api.storage.AvatarService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Inject;

public final class AvatarServiceImpl implements AvatarService {
    private final AccountService accountService;
    private final StorageReference usersPath;

    @Inject
    AvatarServiceImpl(FirebaseStorage storage, AccountService accountService) {
        this.accountService = accountService;
        usersPath = storage.getReference("users");
    }

    @Override
    public void saveAvatar(@NonNull Uri photo,
                           @NonNull Consumer<Uri> onSuccess,
                           @NonNull Consumer<ServiceException> onFailure) {

        if (!validateAvatar(photo)) {
            onFailure.accept(new ServiceException(R.string.error_invalid_photo));
            return;
        }

        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;


        String uploadPhotoName = "avatar";
        StorageReference uploadDestination = usersPath.child(user.id()).child(uploadPhotoName);

        savePhoto(uploadDestination, photo, onSuccess, onFailure);

    }

    /**
     * @param photo the absolute url of the avatar
     * @return true if the photo isn't null, isn't relative, is content or file uri, otherwise false
     */
    @Override
    public boolean validateAvatar(@Nullable Uri photo) {
        if (photo == null) return false;

        String scheme = photo.getScheme();
        if (scheme == null) return false;

        return scheme.equals(ContentResolver.SCHEME_CONTENT) || scheme.equals(ContentResolver.SCHEME_FILE);
    }

    private static void savePhoto(@NonNull StorageReference storageRef,
                                  @NonNull Uri photo,
                                  @NonNull Consumer<Uri> onSuccess,
                                  @NonNull Consumer<ServiceException> onFailure) {
        storageRef
                .putFile(photo)
                .addOnSuccessListener(
                        taskSnapshot -> storageRef.getDownloadUrl()
                                .addOnSuccessListener(onSuccess::accept)
                                .addOnFailureListener(e -> onFailure.accept(new ServiceException(R.string.error_cant_access_photo)))
                )
                .addOnFailureListener(e -> onFailure.accept(new ServiceException(R.string.error_cant_upload_photo,e)));
    }

}
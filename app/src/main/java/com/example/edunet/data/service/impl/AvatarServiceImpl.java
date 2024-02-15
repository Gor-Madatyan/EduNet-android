package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.storage.UriValidator.isValidUploadUri;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.AvatarService;
import com.example.edunet.data.service.exceptions.ServiceException;
import com.example.edunet.data.service.model.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.function.Consumer;

public final class AvatarServiceImpl implements AvatarService {
    private final static FirebaseStorage storage = FirebaseStorage.getInstance();
    private final static AccountService accountService = AccountService.IMPL;
    private final static StorageReference users = storage.getReference().child("users");

    private enum Result {
        SUCCESS,
        EARLY_SUCCESS,
        FAILURE;

        static Result fromBoolean(boolean value) {
            return value ? SUCCESS : FAILURE;
        }
    }

    static {
        assert accountService instanceof AccountServiceImpl : "this AvatarService works only with firebase account service";
    }

    @Override
    public void saveAvatarOfCurrentUser(@Nullable Uri photo,
                                        @Nullable String extension,
                                        @NonNull Consumer<Uri> onSuccess,
                                        @NonNull Consumer<Void> onEarlySuccess,
                                        @NonNull Consumer<ServiceException> onFailure) {
        accountService.reloadUserInfo(e -> {
            if (e != null) {
                onFailure.accept(e);
                return;
            }

            User user = accountService.getCurrentUser();
            assert user != null : AccountService.ErrorMessages.CURRENT_USER_IS_NULL;


            Uri currentPhoto = user.photo();
            Result validation = validateUploadPhotoUri(photo, extension, currentPhoto);

            if (validation == Result.FAILURE) {
                onFailure.accept(new ServiceException("provided invalid photo"));
                return;
            } else if (validation == Result.EARLY_SUCCESS) {
                onEarlySuccess.accept(null);
                return;
            }

            String uploadPhotoName = "avatar" + extension;
            StorageReference uploadDestination = users.child(user.id()).child(uploadPhotoName);

            if (currentPhoto != null && accountService.isUserPhotoDomestic())
                storage.getReferenceFromUrl(currentPhoto.toString())
                        .delete()
                        .addOnFailureListener(
                                e1 -> onFailure.accept(new ServiceException("cant delete current photo", e1))
                        )
                        .addOnSuccessListener(
                                v1 -> guardedSavePhoto(uploadDestination, photo, onSuccess, onFailure)
                        );

            else guardedSavePhoto(uploadDestination, photo, onSuccess, onFailure);

        });


    }

    private static Result validateUploadPhotoUri(@Nullable Uri newPhoto,
                                                 @Nullable String extension,
                                                 @Nullable Uri currentPhoto) {

        if (Objects.equals(currentPhoto, newPhoto)) return Result.EARLY_SUCCESS;

        if (newPhoto != null && extension == null) return Result.FAILURE;

        return Result.fromBoolean(newPhoto == null || isValidUploadUri(newPhoto));
    }

    private static void guardedSavePhoto(@NonNull StorageReference storageRef,
                                         @Nullable Uri photo,
                                         @NonNull Consumer<Uri> onSuccess,
                                         @NonNull Consumer<ServiceException> onFailure) {
        if (photo == null)
            onSuccess.accept(null);
        else savePhoto(storageRef, photo, onSuccess, onFailure);
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
                                .addOnFailureListener(e->onFailure.accept(new ServiceException("cant get new photo url",e)))
                )
                .addOnFailureListener(e-> onFailure.accept(new ServiceException("cant upload photo",e)));
    }

}
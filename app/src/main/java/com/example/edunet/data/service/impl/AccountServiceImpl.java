package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.FirestoreUtils.initializeDocument;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireBaseUser;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccountServiceImpl implements AccountService {
    private static final String TAG = AccountServiceImpl.class.getSimpleName();
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private final AvatarManager avatarManager;
    private final CollectionReference firestoreUsers;
    private DocumentReference userMetadataReference;
    private final MutableLiveData<UserMetadata> userMetadata = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> firebaseUser = new MutableLiveData<>();
    private final MediatorLiveData<User> currentUser = new MediatorLiveData<>();
    private ListenableFuture<Void> userMetadataInitialization;

    {
        currentUser.addSource(firebaseUser, user ->
                currentUser.setValue(userFromFireBaseUser(user, userMetadata.getValue())));

        currentUser.addSource(userMetadata, metadata ->
                currentUser.setValue(userFromFireBaseUser(firebaseUser.getValue(), metadata)));
    }

    private class AvatarManager {
        private final StorageReference users = storage.getReference().child("users");

        public void saveAvatar(@NonNull Uri photo,
                               @NonNull Consumer<Uri> onSuccess,
                               @NonNull Consumer<ServiceException> onFailure) {

            User user = getCurrentUser();
            assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

            String uploadPhotoName = "avatar";
            StorageReference uploadDestination = users.child(user.id()).child(uploadPhotoName);

            StorageUtils.savePhoto(uploadDestination, photo, onSuccess,
                    e -> onFailure.accept(new ServiceException(R.string.error_cant_upload_photo, e))
            );

        }

        public static boolean validateAvatar(@NonNull Uri avatar) {
            return StorageUtils.validatePhoto(avatar);
        }
    }



    public static class UserMetadata {
        private String bio;

        public UserMetadata() {
        }

        public UserMetadata(String bio) {
            this.bio = bio;
        }


        public static UserMetadata getDefault() {
            return new UserMetadata(null);
        }

        public String getBio() {
            return bio;
        }
    }


    @Inject
    AccountServiceImpl(FirebaseAuth auth, FirebaseStorage storage, FirebaseFirestore firestore) {
        this.auth = auth;
        this.storage = storage;
        this.firestoreUsers = firestore.collection("users");
        this.avatarManager = new AvatarManager();

        auth.addAuthStateListener(firebaseAuth -> {
                    FirebaseUser user = auth.getCurrentUser();
                    firebaseUser.setValue(user);

                    if (user != null) {
                        userMetadataReference = firestoreUsers.document(user.getUid());
                        userMetadataInitialization = initializeUserMetadata();

                        userMetadataInitialization.addListener(() -> {
                            try {
                                userMetadataInitialization.get();
                                setUserMetadataListener();
                            } catch (Exception e) {
                                Log.e(TAG, "can't set listener on user metadata");
                            }
                        }, Runnable::run);
                    }

                }
        );
    }

    @Nullable
    @Override
    public String getUid() {
        return auth.getUid();
    }

    @Override
    @NonNull
    public LiveData<User> observeCurrentUser() {
        return currentUser;
    }

    @Override
    @Nullable
    public User getCurrentUser() {
        return userFromFireBaseUser(auth.getCurrentUser(), userMetadata.getValue());
    }

    @Override
    public void updateCurrentUser(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        if (!validateUserUpdate(request)) {
            onResult.accept(new ServiceException(R.string.error_invalid_profile_update_request));
            return;
        }

        if (request.getAvatar() == null) {
            _updateCurrentUser(request, onResult);
            return;
        }

        avatarManager.saveAvatar(request.getAvatar(),
                avatar -> _updateCurrentUser(request.setAvatar(avatar), onResult),
                onResult
        );

    }

    private void _updateCurrentUser(@NonNull UserUpdateRequest abstractRequest,
                                    @NonNull Consumer<ServiceException> onResult) {

        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;

        user.updateProfile(FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest(abstractRequest))
                .addOnSuccessListener(v -> {
                    refreshCurrentUser();

                    if (abstractRequest.isBioSet()) saveBio(abstractRequest.getBio(), onResult);
                    else onResult.accept(null);
                })
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_profile_update, e)));
    }

    @Override
    public boolean validateUserUpdate(@NonNull UserUpdateRequest request) {
        String name = request.getName();
        String bio = request.getBio();
        Uri avatar = request.getAvatar();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;

        if (avatar != null && !AvatarManager.validateAvatar(avatar))
            return false;

        if (((request.isNameSet() || user.getDisplayName() == null)) && name == null)
            return false;

        if (name != null) request.setName(name = name.trim());

        if (bio != null) {
            bio = bio.trim();
            if (bio.isEmpty()) request.setBio(null);
            else request.setBio(bio);
        }
        return name == null || !name.isEmpty();
    }

    @Override
    public void signOut() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        if (user.isAnonymous()) user.delete();
        auth.signOut();
    }

    private ListenableFuture<Void> initializeUserMetadata() {
        return initializeDocument(userMetadataReference, UserMetadata.getDefault());
    }

    private void setUserMetadataListener() {
        userMetadataReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "can't fetch metadata");
                return;
            }
            assert snapshot != null;
            UserMetadata metadata = Objects.requireNonNull(snapshot.toObject(UserMetadata.class));
            userMetadata.setValue(metadata);
        });
    }

    private void saveBio(String bio, Consumer<ServiceException> onResult) {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;

        userMetadataInitialization.addListener(() -> {
            try {
                userMetadataInitialization.get();
            } catch (Exception e) {
                onResult.accept(new ServiceException(R.string.error_bio_update, e));
                return;
            }
            userMetadataReference.update("bio", bio)
                    .addOnCompleteListener(r -> {
                        Exception e = r.getException();
                        onResult.accept(e == null ? null : new ServiceException(R.string.error_bio_update, e));
                    });
        }, Runnable::run);


    }

    private void refreshCurrentUser() {
        firebaseUser.setValue(auth.getCurrentUser());
    }

}

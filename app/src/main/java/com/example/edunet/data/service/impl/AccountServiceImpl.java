package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.FirestoreUtils.initializeDocument;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.FireBaseUserProfileChangeRequestFromAbstractUserUpdateRequest;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireBaseUser;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccountServiceImpl implements AccountService {
    private static final String TAG = AccountServiceImpl.class.getSimpleName();
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private final CommunityServiceImpl.CommunityUtils communityUtils;
    private final AvatarManager avatarManager;
    private DocumentReference userMetadataReference;
    private final MutableLiveData<ProcessedUserMetadata> userMetadata = new MutableLiveData<>();
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

    private static class RawUserMetadata {
        private String bio;

        private List<String> ownedCommunities;

        public RawUserMetadata() {
        }

        public RawUserMetadata(String bio, List<String> ownedCommunities) {
            this.bio = bio;
            this.ownedCommunities = ownedCommunities;
        }


        public static RawUserMetadata getDefault() {
            return new RawUserMetadata(null, new ArrayList<>());
        }

        public List<String> getOwnedCommunities() {
            return ownedCommunities;
        }

        public String getBio() {
            return bio;
        }
    }

    public static class ProcessedUserMetadata {
        private String bio;

        private List<Pair<String, Community>> ownedCommunities;

        public ProcessedUserMetadata() {
        }

        public ProcessedUserMetadata(String bio, List<Pair<String, Community>> ownedCommunities) {
            this.bio = bio;
            this.ownedCommunities = ownedCommunities;
        }

        public static ProcessedUserMetadata getPlaceholder() {
            return new ProcessedUserMetadata(null, new ArrayList<>());
        }

        public List<Pair<String, Community>> getOwnedCommunities() {
            return ownedCommunities;
        }

        public String getBio() {
            return bio;
        }
    }

    @Inject
    AccountServiceImpl(FirebaseAuth auth, CommunityServiceImpl.CommunityUtils communityUtils, FirebaseStorage storage, FirebaseFirestore firestore) {
        this.auth = auth;
        this.communityUtils = communityUtils;
        this.storage = storage;
        this.avatarManager = new AvatarManager();

        auth.addAuthStateListener(firebaseAuth -> {
                    FirebaseUser user = auth.getCurrentUser();
                    firebaseUser.setValue(user);

                    if (user != null) {
                        userMetadataReference = firestore.collection("users").document(user.getUid());
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

    // FIXME: 3/5/2024 there may be case when metadata initialization can
    //                fail, but user can create an group that is not attached to him

    void attachCommunity(@NonNull String id, @NonNull Community.Role role, @NonNull Consumer<ServiceException> onResult) {
        userMetadataInitialization.addListener(
                () -> {
                    try {
                        userMetadataInitialization.get();
                        userMetadataReference.update("ownedCommunities", FieldValue.arrayUnion(id))
                                .addOnSuccessListener(r -> onResult.accept(null))
                                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_attach_community, e)));
                    } catch (Exception e) {
                        onResult.accept(new ServiceException(R.string.error_cant_attach_community, e));
                    }

                }, Runnable::run);
    }

    @Override
    public void signOut() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        if (user.isAnonymous()) user.delete();
        auth.signOut();
    }

    private ListenableFuture<Void> initializeUserMetadata() {
        return initializeDocument(userMetadataReference, RawUserMetadata.getDefault());
    }

    private void setUserMetadataListener() {
        userMetadataReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "can't fetch metadata");
                return;
            }
            assert snapshot != null;
            RawUserMetadata metadata = Objects.requireNonNull(snapshot.toObject(RawUserMetadata.class));

            parseMetadata(metadata,
                    userMetadata::setValue,
                    e1 -> Log.e(TAG, "can't parse metadata")
            );
        });
    }

    private void parseMetadata(@NonNull RawUserMetadata rawMetadata,
                               @NonNull Consumer<ProcessedUserMetadata> onSuccess,
                               @NonNull Consumer<ServiceException> onFailure) {

        String bio = rawMetadata.bio;
        List<String> rawOwnedCommunities = rawMetadata.ownedCommunities;

        communityUtils.loadCommunities(rawOwnedCommunities,
                parsedCommunities ->
                        onSuccess.accept(new ProcessedUserMetadata(bio, parsedCommunities)),
                onFailure);

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

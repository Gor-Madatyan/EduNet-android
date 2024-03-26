package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromAuthUser;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireStoreUser;

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
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.common.Paginator;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.example.edunet.data.service.util.firebase.paginator.ArrayPaginator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AccountServiceImpl implements AccountService {
    private static final String TAG = AccountServiceImpl.class.getSimpleName();
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private final AvatarManager avatarManager;
    private final CollectionReference firestoreUsers;
    private DocumentReference firestoreUserReference;
    private final MutableLiveData<FirestoreUser> firestoreUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> firebaseUserLiveData = new MutableLiveData<>();
    private ListenerRegistration firestoreUserListener;
    private final MediatorLiveData<User> currentUserLiveData = new MediatorLiveData<>();

    {
        currentUserLiveData.addSource(firebaseUserLiveData, user ->
                currentUserLiveData.setValue(userFromAuthUser(user)));

        currentUserLiveData.addSource(firestoreUserLiveData, firestoreUser ->
                currentUserLiveData.setValue(userFromFireStoreUser(getUid(), firestoreUser)));
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

    public static class FirestoreUser {
        private String name;
        private String avatar;
        private String bio;

        public FirestoreUser(String name, String avatar, String bio) {
            this.bio = bio;
            this.name = name;
            this.avatar = avatar;
        }

        @SuppressWarnings("unused")
        public FirestoreUser() {
        }

        public String getName() {
            return name;
        }

        public String getAvatar() {
            return avatar;
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
                    firebaseUserLiveData.setValue(user);

                    if (user != null) {
                        firestoreUserReference = firestoreUsers.document(user.getUid());
                        if (firestoreUserListener != null) firestoreUserListener.remove();
                        firestoreUserListener = setFirestoreUserListener();
                    }

                }
        );
    }

    private void initialize(@NonNull Consumer<ServiceException> onResult) {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        DocumentReference userPath = firestoreUsers.document(Objects.requireNonNull(getUid()));

        userPath.set(new FirestoreUser(
                        user.getDisplayName(),
                        user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString(),
                        null))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_initialize_user, e)));
    }

    @Nullable
    @Override
    public String getUid() {
        return auth.getUid();
    }

    @Override
    public boolean isUserAvailable() {
        return auth.getCurrentUser() != null;
    }

    @Override
    @NonNull
    public LiveData<User> observeCurrentUser() {
        return currentUserLiveData;
    }

    @Override
    @Nullable
    public User getCurrentUser() {
        return currentUserLiveData.getValue();
    }

    @Override
    public Paginator<Pair<String, User>> getUserArrayPaginator(String[] uids, int limit) {
        DocumentReference[] references = new DocumentReference[uids.length];

        for (int i = 0; i < uids.length; i++) {
            references[i] = firestoreUsers.document(uids[i]);
        }

        ArrayPaginator<FirestoreUser> paginator = new ArrayPaginator<>(FirestoreUser.class, references, limit);

        return new Paginator<>() {
            @SuppressWarnings("all")
            @Override
            public void next(Consumer<List<Pair<String, User>>> onSuccess, Consumer<Exception> onFailure) {
                paginator.next(
                        users -> {
                            List<Pair<String,User>> parsedUsers =
                                    users.stream().map(pair -> new Pair<>(pair.first, userFromFireStoreUser(pair.first, pair.second)))
                                            .collect(Collectors.toList());
                            onSuccess.accept(parsedUsers);
                        },
                        e -> onFailure.accept(new ServiceException(R.string.error_cant_load_user, e))
                );
            }

            @Override
            public boolean hasFailure() {
                return paginator.hasFailure();
            }

            @Override
            public boolean isEofReached() {
                return paginator.isEofReached();
            }
        };
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

    private void _updateCurrentUser(@NonNull UserUpdateRequest request,
                                    @NonNull Consumer<ServiceException> onResult) {
        firestoreUserReference.update(request.toMap())
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_profile_update, e)));
    }

    @Override
    public boolean validateUserUpdate(@NonNull UserUpdateRequest request) {
        if (!firestoreUserLiveData.isInitialized())
            return false;

        String name = request.getName();
        String bio = request.getBio();
        Uri avatar = request.getAvatar();

        if (avatar != null && !AvatarManager.validateAvatar(avatar))
            return false;

        if (request.isNameSet() && name == null)
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
        auth.signOut();
    }

    private ListenerRegistration setFirestoreUserListener() {
        Log.i(TAG, "user listener was set");
        return firestoreUserReference.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, e.toString());
                return;
            }
            assert snapshot != null;
            if (!snapshot.exists()) {
                initialize(e1 -> Log.w(TAG, e1));
                return;
            }

            firestoreUserLiveData.setValue(snapshot.toObject(FirestoreUser.class));
        });
    }

}

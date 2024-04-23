package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromAuthUser;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireStoreUser;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.example.edunet.data.service.util.firebase.paginator.ArrayPaginator;
import com.example.edunet.data.service.util.paginator.Paginator;
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
            StorageReference uploadDestination = users.child(user.getId()).child(uploadPhotoName);

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

        if (isUserAvailable()) onSignIn();
        auth.addAuthStateListener(firebaseAuth -> firebaseUserLiveData.setValue(auth.getCurrentUser()));
    }

    private void initialize(@NonNull Consumer<ServiceException> onResult) {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null : InternalErrorMessages.CURRENT_USER_IS_NULL;
        DocumentReference userPath = firestoreUsers.document(Objects.requireNonNull(user.getUid()));

        userPath.set(new FirestoreUser(
                        user.getDisplayName(),
                        user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString(),
                        null))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_initialize_user, e)));
    }

    @Override
    public void onSignIn() {
        firestoreUserReference = firestoreUsers.document(Objects.requireNonNull(getUid()));
        firestoreUserListener = setFirestoreUserListener();
    }

    @Nullable
    @Override
    public String getUid() {
        return auth.getUid();
    }

    public DocumentReference getUserDocumentById(@NonNull String uid){
        return firestoreUsers.document(uid);
    }

    @Override
    public void getUserById(@NonNull String uid, @NonNull Consumer<User> onSuccess, @NonNull Consumer<ServiceException> onFailure) {
        firestoreUsers.document(uid).get()
                .addOnSuccessListener(snapshot -> onSuccess.accept(userFromFireStoreUser(uid, snapshot.toObject(FirestoreUser.class))))
                .addOnFailureListener(e -> onFailure.accept(new ServiceException(R.string.error_cant_load_user, e)));
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

    @NonNull
    @Override
    public LiveData<User> observeUser(@NonNull LifecycleOwner owner, @NonNull String uid) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        FirestoreUtils.attachObserver(
                firestoreUsers.document(uid).addSnapshotListener((snapshot,err)->{
                    if(err != null){
                        Log.w(TAG,err);
                        return;
                    }
                    assert snapshot != null;
                    liveData.setValue(userFromFireStoreUser(snapshot.getId(), snapshot.toObject(FirestoreUser.class)));
                }),
                owner
        );

        return liveData;
    }

    @Override
    @Nullable
    public User getCurrentUser() {
        return currentUserLiveData.getValue();
    }

    @Override
    public Paginator<User> getUserArrayPaginator(String[] uids, int limit) {
        DocumentReference[] references = new DocumentReference[uids.length];

        for (int i = 0; i < uids.length; i++) {
            references[i] = firestoreUsers.document(uids[i]);
        }

        ArrayPaginator<FirestoreUser> in = new ArrayPaginator<>(FirestoreUser.class, references, limit);

        return new Paginator<>() {
            @SuppressWarnings("all")
            @Override
            public void next(Consumer<List<User>> onSuccess, Consumer<Exception> onFailure) {
                in.next(
                        users -> {
                            onSuccess.accept(
                                    users.stream().map(pair -> userFromFireStoreUser(pair.first, pair.second))
                                            .collect(Collectors.toList())
                            );
                        },
                        e -> onFailure.accept(new ServiceException(R.string.error_cant_load_user, e))
                );
            }

            @Override
            public boolean isLoading() {
                return in.isLoading();
            }

            @Override
            public boolean hasFailure() {
                return in.hasFailure();
            }

            @Override
            public boolean isEofReached() {
                return in.isEofReached();
            }
        };
    }

    @Override
    public void updateCurrentUser(@NonNull UserUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        if (isUserUpdateInvalid(request)) {
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
    public boolean isUserUpdateInvalid(@NonNull UserUpdateRequest request) {
        if (!firestoreUserLiveData.isInitialized())
            return true;

        String name = request.getName();
        String bio = request.getBio();
        Uri avatar = request.getAvatar();

        if (avatar != null && !AvatarManager.validateAvatar(avatar))
            return true;

        if (request.isNameSet() && name == null)
            return true;

        if (name != null) request.setName(name = name.trim());

        if (bio != null) {
            bio = bio.trim();
            if (bio.isEmpty()) request.setBio(null);
            else request.setBio(bio);
        }
        return name != null && name.isEmpty();
    }

    @Override
    public void signOut() {
        firestoreUserListener.remove();
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

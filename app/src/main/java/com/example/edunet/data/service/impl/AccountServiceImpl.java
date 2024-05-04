package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromAuthUser;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireStoreUser;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.PatternsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.EmailCredential;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.model.UserUpdateRequest;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.example.edunet.data.service.util.firebase.paginator.ArrayPaginator;
import com.example.edunet.data.service.util.paginator.Paginator;
import com.example.edunet.util.UriUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
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
        private String email;
        private String avatar;
        private String bio;

        public FirestoreUser(String name, String email, String avatar, String bio) {
            this.bio = bio;
            this.name = name;
            this.email = email;
            this.avatar = avatar;
        }

        @SuppressWarnings("unused")
        public FirestoreUser() {
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
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
                        user.getEmail(),
                        UriUtils.safeToString(user.getPhotoUrl()),
                        null))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_initialize_user, e)));
    }

    private void onSignIn() {
        firestoreUserReference = firestoreUsers.document(Objects.requireNonNull(getUid()));
        firestoreUserListener = setFirestoreUserListener();
    }

    private void handleSignIn(@NonNull Task<?> task, @NonNull Consumer<ServiceException> onResult) {
        task.addOnFailureListener(e -> {
                    Log.e(TAG, "sign in failed", e);
                    onResult.accept(new ServiceException(R.string.error_sign_in, e));
                })
                .addOnSuccessListener(v -> {
                    onSignIn();
                    onResult.accept(null);
                });
    }

    @Override
    public boolean validateEmail(@NonNull String email) {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean validateName(@NonNull String name) {
        return !name.trim().isEmpty();
    }

    @Override
    public boolean validatePassword(@NonNull String password) {
        password = password.trim();
        return password.length() >= 6 && password.matches(".*\\d+.*");
    }

    @Override
    public boolean validateEmailCredentials(@NonNull EmailCredential credentials) {
        return validateEmail(credentials.email()) && validateName(credentials.name()) && validatePassword(credentials.password());
    }


    @Override
    public void signInWithGoogleIdToken(@NonNull String idToken, @NonNull Consumer<ServiceException> onResult) {
        handleSignIn(auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)), onResult);
    }

    @Override
    public void signInWithEmailAddress(@NonNull String email, @NonNull String password, @NonNull Consumer<ServiceException> onResult) {
        if (!validateEmail(email) || !validatePassword(password)) {
            onResult.accept(new ServiceException(R.string.error_invalid_email_credentials));
            return;
        }

        handleSignIn(auth.signInWithEmailAndPassword(email, password), onResult);
    }

    @Override
    public void signUpWithEmailAddress(@NonNull EmailCredential emailCredential, @NonNull Consumer<ServiceException> onResult) {
        if (!validateEmailCredentials(emailCredential)) {
            onResult.accept(new ServiceException(R.string.error_invalid_email_credentials));
            return;
        }
        auth.createUserWithEmailAndPassword(emailCredential.email(), emailCredential.password())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "cant sign up with email", e);
                    onResult.accept(new ServiceException(R.string.error_sign_up, e));
                })
                .addOnSuccessListener(v ->
                        Objects.requireNonNull(auth.getCurrentUser()).updateProfile(
                                new UserProfileChangeRequest.Builder().setDisplayName(emailCredential.name()).build()
                        ).addOnCompleteListener(
                                r -> {
                                    if(r.getException() != null)
                                        Log.e(TAG, "error setting user name", r.getException());
                                    onSignIn();
                                    onResult.accept(null);
                                }
                        ));
    }

    @Override
    public void sendEmailVerification(@NonNull Consumer<ServiceException> onResult) {
        Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification()
                .addOnSuccessListener(v -> {
                    Log.i(TAG, "email verification sent");
                    onResult.accept(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "cant send email verification", e);
                    onResult.accept(new ServiceException(R.string.error_send_email_verification, e));
                });
    }

    @Override
    public void sendPasswordResetEmail(@NonNull String email, @NonNull Consumer<ServiceException> onResult) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v->{
                    Log.i(TAG, "password reset email sent");
                    onResult.accept(null);
                })
                .addOnFailureListener(e->{
                    Log.e(TAG, "cant send password reset email", e);
                    onResult.accept(new ServiceException(R.string.error_send_password_reset_email, e));
                });
    }

    @Nullable
    @Override
    public String getUid() {
        return auth.getUid();
    }

    public DocumentReference getUserDocumentById(@NonNull String uid) {
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
    public boolean isCurrentUserEmailVerified() {
        return Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified();
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
                firestoreUsers.document(uid).addSnapshotListener((snapshot, err) -> {
                    if (err != null) {
                        Log.w(TAG, err);
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

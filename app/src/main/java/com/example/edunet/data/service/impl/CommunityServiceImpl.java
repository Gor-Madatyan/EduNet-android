package com.example.edunet.data.service.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class CommunityServiceImpl implements CommunityService {
    private final AvatarManager avatarManager;
    private final AccountServiceImpl accountService;
    private final CollectionReference communityCollection;
    private final FirebaseStorage storage;


    private final class AvatarManager {
        private final StorageReference communities = storage.getReference("communities");

        public void saveAvatar(@NonNull Uri photo,
                               @NonNull String id,
                               @NonNull Consumer<Uri> onSuccess,
                               @NonNull Consumer<ServiceException> onFailure) {

            String uploadPhotoName = "avatar";
            StorageReference uploadDestination = communities
                    .child(id)
                    .child(uploadPhotoName);

            StorageUtils.savePhoto(uploadDestination, photo, onSuccess,
                    e -> onFailure.accept(new ServiceException(R.string.error_cant_upload_photo, e))
            );

        }

        public void cleanUp(@NonNull String id, @NonNull Consumer<ServiceException> onResult) {
            StorageReference path = communities.child(id).child("avatar");

            path.delete()
                    .addOnSuccessListener(r -> onResult.accept(null))
                    .addOnFailureListener(
                            e -> {
                                StorageException storageException = (StorageException) e;
                                onResult.accept(
                                        storageException.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND ? null
                                                : new ServiceException(R.string.error_cant_cleanup_community_photos, e)
                                );
                            }
                    );
        }

        public static boolean validateAvatar(@NonNull Uri avatar) {
            return StorageUtils.validatePhoto(avatar);
        }
    }


    @Inject
    CommunityServiceImpl(AccountServiceImpl accountService, FirebaseStorage storage, FirebaseFirestore firestore) {
        this.accountService = accountService;
        this.storage = storage;
        this.communityCollection = firestore.collection("communities");
        avatarManager = new AvatarManager();
    }

    @Override
    public void createCommunity(@NonNull CommunityCreateRequest request, @NonNull Consumer<ServiceException> onResult) {

        if (!validateCommunityCreateRequest(request)) {
            onResult.accept(new ServiceException(R.string.error_invalid_community_create_request));
            return;
        }
        DocumentReference community = communityCollection.document();

        if (request.getAvatar() == null) {
            _createCommunity(community, request, onResult);
            return;
        }

        avatarManager.saveAvatar(request.getAvatar(), community.getId(),
                avatar -> _createCommunity(community, request.setAvatar(avatar), onResult),
                onResult
        );
    }

    @Override
    public void deleteCommunity(@NonNull String id, Consumer<ServiceException> onResult) {
        DocumentReference community = communityCollection.document(id);

        avatarManager.cleanUp(id,
                e -> {
                    if (e != null) {
                        onResult.accept(e);
                        return;
                    }
                    accountService.detachOwnedCommunity(id,
                            e1 -> {
                                if (e1 != null) {
                                    onResult.accept(e1);
                                    return;
                                }
                                community.delete()
                                        .addOnSuccessListener(r -> onResult.accept(null))
                                        .addOnFailureListener(
                                                e2 ->
                                                    onResult.accept(new ServiceException(R.string.error_cant_delete_community, e2))
                                        );
                            }
                    );

                }
        );
    }

    @Override
    public boolean validateCommunityCreateRequest(@NonNull CommunityCreateRequest request) {
        if (request.getAvatar() != null && !AvatarManager.validateAvatar(request.getAvatar()))
            return false;

        String name = request.getName();
        String description = request.getDescription();

        if (name == null || description == null)
            return false;

        request.setName(name = request.getName().trim());
        request.setDescription(description = request.getDescription().trim());

        return !name.isEmpty() && !description.isEmpty();
    }

    private void _createCommunity(@NonNull DocumentReference community,
                                  @NonNull CommunityCreateRequest request,
                                  @NonNull Consumer<ServiceException> onResult) {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

        community.set(new Community(request.getName(),
                        request.getDescription(),
                        request.getAvatar() == null ? null : request.getAvatar().toString(),
                        user.id()))
                .addOnSuccessListener(r -> accountService.attachCommunity(community.getId(), Community.Role.OWNER, onResult))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_create_community, e)));
    }

    @Override
    public void updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        if (!validateCommunityUpdateRequest(request)) {
            onResult.accept(new ServiceException(R.string.error_invalid_community_update_request));
            return;
        }

        if (request.getAvatar() == null) {
            _updateCommunity(request, onResult);
            return;
        }

        avatarManager.saveAvatar(request.getAvatar(), request.getId(),
                photo -> _updateCommunity(request.setAvatar(photo), onResult),
                onResult
        );
    }

    private void _updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        DocumentReference document = communityCollection.document(request.getId());
        Map<String, Object> map = new HashMap<>();

        if (request.isAvatarSet()) map.put("avatar", request.getAvatar());
        if (request.isNameSet()) map.put("name", request.getName());
        if (request.isDescriptionSet()) map.put("description", request.getDescription());

        document.update(map)
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_update_community, e)));
    }

    @Override
    public boolean validateCommunityUpdateRequest(@NonNull CommunityUpdateRequest request) {
        if (request.getAvatar() != null && !AvatarManager.validateAvatar(request.getAvatar()))
            return false;

        String name = request.getName();
        String description = request.getDescription();

        if ((request.isNameSet() && name == null) || (request.isDescriptionSet() && description == null))
            return false;

        if (request.isNameSet()) {
            request.setName(name = name.trim());
            if (name.isEmpty()) return false;
        }

        if (request.isDescriptionSet()) {
            request.setDescription(description = description.trim());
            if (description.isEmpty()) return false;
        }

        return true;
    }

    @Override
    public void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id, @NonNull BiConsumer<Community, ServiceException> listener) {
        ListenerRegistration listenerRegistration = communityCollection.document(id).addSnapshotListener(
                (snapshot, e) -> {
                    if (e != null || !Objects.requireNonNull(snapshot).exists()) {
                        listener.accept(null, new ServiceException(R.string.error_cant_load_community, e));
                        return;
                    }

                    listener.accept(snapshot.toObject(Community.class), null);
                }
        );

        FirestoreUtils.attachObserver(listenerRegistration, lifecycleOwner);
    }

    @Override
    public void loadCommunities(@NonNull List<String> communityIds,
                                @NonNull Consumer<List<Pair<String, Community>>> onSuccess,
                                @NonNull Consumer<ServiceException> onFailure) {
        List<DocumentReference> documents = communityIds.stream().map(communityCollection::document).collect(Collectors.toList());

        FirestoreUtils.loadData(Community.class, documents,
                entries ->
                        onSuccess.accept(entries.stream().map(el -> new Pair<>(el.first.getId(), el.second)).collect(Collectors.toList())),
                e -> onFailure.accept(new ServiceException(R.string.error_cant_load_community, e)));
    }

}

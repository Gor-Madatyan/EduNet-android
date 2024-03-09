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
import com.example.edunet.data.service.model.CommunityModifyRequest;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class CommunityServiceImpl implements CommunityService {
    private final AvatarManager avatarManager;
    private final AccountServiceImpl accountService;
    private final CollectionReference communityCollection;
    private final FirebaseStorage storage;

    static class CommunityUtils {

        private final CollectionReference communityCollection;

        @Inject
        CommunityUtils(FirebaseFirestore firestore) {
            communityCollection = firestore.collection("communities");
        }

        public void loadCommunities(@NonNull List<String> communityIds,
                                    @NonNull Consumer<List<Pair<String, Community>>> onSuccess,
                                    @NonNull Consumer<ServiceException> onFailure) {
            List<DocumentReference> documents = communityIds.stream().map(communityCollection::document).collect(Collectors.toList());

            FirestoreUtils.loadData(Community.class, documents,
                    map -> onSuccess.accept(map.stream()
                            .map(community -> new Pair<>(community.first.getId(), community.second))
                            .collect(Collectors.toList())),

                    e -> onFailure.accept(new ServiceException(R.string.error_cant_load_community, e)));
        }
    }

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
    public void createCommunity(@NonNull CommunityModifyRequest request, @NonNull Consumer<ServiceException> onResult) {

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
    public boolean validateCommunityCreateRequest(@NonNull CommunityModifyRequest request) {
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

    @Override
    public void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id, @NonNull BiConsumer<Community, ServiceException> listener) {
        ListenerRegistration listenerRegistration = communityCollection.document(id).addSnapshotListener(
                (snapshot, e) -> {
                    if (e != null) {
                        listener.accept(null, new ServiceException(R.string.error_cant_load_community, e));
                        return;
                    }

                    assert snapshot != null;
                    listener.accept(snapshot.toObject(Community.class), null);
                }
        );

        FirestoreUtils.attachObserver(listenerRegistration, lifecycleOwner);
    }

    private void _createCommunity(@NonNull DocumentReference community,
                                  @NonNull CommunityModifyRequest request,
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
}

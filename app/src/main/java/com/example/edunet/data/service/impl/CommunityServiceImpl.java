package com.example.edunet.data.service.impl;

import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.communityFromFirestoreCommunity;
import static com.example.edunet.data.service.util.firebase.typeconversion.FirebaseTypeConversionUtils.userFromFireStoreUser;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.R;
import com.example.edunet.util.UriUtils;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.firebase.FirestoreUtils;
import com.example.edunet.data.service.util.firebase.StorageUtils;
import com.example.edunet.data.service.util.firebase.paginator.PortionedPaginator;
import com.example.edunet.data.service.util.firebase.paginator.QueryPaginator;
import com.example.edunet.data.service.util.paginator.Paginator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class CommunityServiceImpl implements CommunityService {
    private static final String TAG = CommunityServiceImpl.class.getSimpleName();
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

        public static boolean isAvatarInvalid(@NonNull Uri avatar) {
            return !StorageUtils.validatePhoto(avatar);
        }
    }

    @SuppressWarnings("unused")
    public final static class FirestoreCommunity {
        private String name;
        private String searchName;
        private String avatar;
        private String description;
        private String ancestor;
        private String ownerId;
        private List<String> admins;
        private List<String> adminsQueue;
        private List<String> participants;
        private List<String> participantsQueue;
        private List<String> graduated;
        private Map<String, List<String>> graduations;

        public FirestoreCommunity() {
        }

        public FirestoreCommunity(@NonNull String name,
                                  @NonNull String description,
                                  @Nullable String avatar,
                                  @Nullable String ancestor,
                                  @NonNull String ownerId) {
            this.name = name;
            searchName = name.toLowerCase(Locale.ROOT);
            this.avatar = avatar;
            this.description = description;
            this.ancestor = ancestor;
            this.ownerId = ownerId;
            admins = new ArrayList<>();
            participants = new ArrayList<>();
            adminsQueue = new ArrayList<>();
            participantsQueue = new ArrayList<>();
            graduated = new ArrayList<>();
            graduations = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public String getSearchName() {
            return searchName;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getDescription() {
            return description;
        }

        public String getAncestor() {
            return ancestor;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public List<String> getAdmins() {
            return admins;
        }

        public List<String> getAdminsQueue() {
            return adminsQueue;
        }

        public List<String> getParticipants() {
            return participants;
        }

        public List<String> getParticipantsQueue() {
            return participantsQueue;
        }

        public List<String> getGraduated() {
            return graduated;
        }

        public Map<String, List<String>> getGraduations() {
            return graduations;
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
    public void observeAttachedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.where(Filter.or(
                        Filter.equalTo("ownerId", uid),
                        Filter.arrayContains("admins", uid),
                        Filter.arrayContains("participants", uid)
                )),
                biConsumer);
    }

    @Override
    public void observeOwnedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.whereEqualTo("ownerId", uid),
                biConsumer);
    }

    @Override
    public void observeAdminedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.whereArrayContains("admins", uid),
                biConsumer);
    }

    @Override
    public void observeParticipatedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.whereArrayContains("participants", uid),
                biConsumer);
    }

    @Override
    public void observeGraduatedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.whereArrayContains("graduated", uid),
                biConsumer);
    }

    @Override
    public void observeSubCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        observeCommunities(lifecycleOwner,
                communityCollection.whereEqualTo("ancestor", cid),
                biConsumer);
    }

    private static void observeCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull Query query, @NonNull BiConsumer<ServiceException, Community[]> biConsumer) {
        FirestoreUtils.attachObserver(
                FirestoreUtils.observeData(
                        query,
                        FirestoreCommunity.class,
                        (e, data) -> {
                            if (e != null) {
                                Log.e(TAG, e.toString());
                                biConsumer.accept(new ServiceException(R.string.error_cant_load_community, e), null);
                            } else {
                                Community[] communities = Arrays.stream(data).map(pair -> communityFromFirestoreCommunity(pair.first, pair.second))
                                        .toArray(Community[]::new);

                                biConsumer.accept(null, communities);
                            }
                        }),
                lifecycleOwner
        );
    }

    @Override
    public void createCommunity(@NonNull CommunityCreateRequest request, @NonNull Consumer<ServiceException> onResult) {

        if (isCommunityCreateRequestInvalid(request)) {
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

    @NonNull
    @Override
    public Paginator<Community> getCommunityPaginator(String namePrefix, int limit) {
        namePrefix = namePrefix.toLowerCase(Locale.ROOT);

        QueryPaginator<FirestoreCommunity> in = new QueryPaginator<>(
                communityCollection.orderBy("searchName")
                        .whereEqualTo("ancestor", null)
                        .startAt(namePrefix)
                        .endAt(namePrefix + '\uf8ff'), limit, FirestoreCommunity.class);

        return new Paginator<>() {
            @Override
            public void next(Consumer<List<Community>> onSuccess, Consumer<Exception> onFailure) {
                in.next(data ->
                                onSuccess.accept(
                                        data.stream().map(pair -> communityFromFirestoreCommunity(pair.first, pair.second))
                                                .collect(Collectors.toList())
                                ),
                        e -> {
                            onFailure.accept(new ServiceException(R.string.error_cant_load_community, e));
                            Log.e(TAG, e.toString());
                        }
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

    @NonNull
    public Paginator<Object> getGraduationsEntryPaginator(@NonNull Pair<String, String[]>[] entries) {
        DocumentReference[][] referencePortions = Arrays.stream(entries).map(
                p -> Arrays.stream(p.second).map(accountService::getUserDocumentById).toArray(DocumentReference[]::new)
        ).toArray(DocumentReference[][]::new);

        Paginator<Pair<String, AccountServiceImpl.FirestoreUser>> in = new PortionedPaginator<>(AccountServiceImpl.FirestoreUser.class, referencePortions);

        return new Paginator<>() {
            private int portion = 0;

            @Override
            public void next(Consumer<List<Object>> onSuccess, Consumer<Exception> onFailure) {
                final int currentPortion = portion;
                in.next(
                        list -> {
                            List<Object> parsedList = list.stream().map(p -> (Object) userFromFireStoreUser(p.first, p.second)).collect(Collectors.toList());
                            parsedList.add(0, entries[currentPortion].first);
                            onSuccess.accept(parsedList);
                        },
                        e ->
                                onFailure.accept(new ServiceException(R.string.error_cant_load_user, e))
                );
                portion++;
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
    public void deleteCommunity(@NonNull String id, Consumer<ServiceException> onResult) {
        DocumentReference community = communityCollection.document(id);

        avatarManager.cleanUp(id,
                e -> {
                    if (e != null) {
                        onResult.accept(e);
                        return;
                    }
                    community.delete()
                            .addOnSuccessListener(r -> onResult.accept(null))
                            .addOnFailureListener(
                                    e1 ->
                                            onResult.accept(new ServiceException(R.string.error_cant_delete_community, e1))
                            );

                }
        );
    }

    @Override
    public void requestAdminPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult) {
        addRequest(Role.ADMIN, cid, onResult);
    }

    @Override
    public void requestParticipantPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult) {
        addRequest(Role.PARTICIPANT, cid, onResult);
    }

    @Override
    public void deleteAdmin(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult) {
        deleteMember(Role.ADMIN, cid, uid, onResult);
    }

    @Override
    public void deleteParticipant(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult) {
        deleteMember(Role.PARTICIPANT, cid, uid, onResult);
    }

    @Override
    public void graduateParticipants(@NonNull String cid, @NonNull String[] uids, @NonNull Consumer<ServiceException> onResult) {
        DocumentReference community = communityCollection.document(cid);
        String date = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().toString();

        community.update("participants", FieldValue.arrayRemove((Object[]) uids),
                        "graduated", FieldValue.arrayUnion((Object[]) uids),
                        "graduations." + date, FieldValue.arrayUnion((Object[]) uids)
                )
                .addOnSuccessListener(v -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_manage_permissions, e)));
    }

    private void deleteMember(@NonNull Role role, @NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;

        DocumentReference community = communityCollection.document(cid);
        community.update(getPermissionsByRole(role), FieldValue.arrayRemove(uid))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_delete_member, e)));
    }

    private void addRequest(@NonNull Role role, @NonNull String cid, @NonNull Consumer<ServiceException> onResult) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;
        String uid = accountService.getUid();
        assert uid != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

        DocumentReference community = communityCollection.document(cid);
        community.update(getPermissionsByRole(role) + "Queue", FieldValue.arrayUnion(uid))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_request_permissions, e)));
    }

    @Override
    public void managePermissions(@NonNull Role role, boolean accept, @NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;
        DocumentReference communityDocument = communityCollection.document(cid);
        String permission = getPermissionsByRole(role);

        Map<String, Object> map = new HashMap<>();
        map.put(permission + "Queue", FieldValue.arrayRemove(uid));
        if (accept) map.put(permission, FieldValue.arrayUnion(uid));

        communityDocument.update(map)
                .addOnSuccessListener(v -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_manage_permissions, e)));
    }

    private static String getPermissionsByRole(@NonNull Role role) {
        return switch (role) {
            case OWNER -> "ownerId";
            case ADMIN -> "admins";
            case PARTICIPANT -> "participants";
            case GRADUATED -> "graduated";
            default -> null;
        };
    }

    @Override
    public boolean isCommunityCreateRequestInvalid(@NonNull CommunityCreateRequest request) {
        if (request.getAvatar() != null && AvatarManager.isAvatarInvalid(request.getAvatar()))
            return true;

        String name = request.getName();
        String description = request.getDescription();

        if (name == null || description == null)
            return true;

        request.setName(name = request.getName().trim());
        request.setDescription(description = request.getDescription().trim());

        return name.isEmpty() || description.isEmpty();
    }

    private void _createCommunity(@NonNull DocumentReference community,
                                  @NonNull CommunityCreateRequest request,
                                  @NonNull Consumer<ServiceException> onResult) {
        User user = accountService.getCurrentUser();
        assert user != null : AccountService.InternalErrorMessages.CURRENT_USER_IS_NULL;

        community.set(new FirestoreCommunity(request.getName(),
                        request.getDescription(),
                        UriUtils.safeToString(request.getAvatar()),
                        request.getAncestor(),
                        user.getId()))
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_create_community, e)));
    }

    @Override
    public void updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult) {
        if (isCommunityUpdateRequestInvalid(request)) {
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
        if (request.isNameSet()) {
            map.put("name", request.getName());
            map.put("searchName", request.getName().toLowerCase(Locale.ROOT));
        }
        if (request.isDescriptionSet()) map.put("description", request.getDescription());

        document.update(map)
                .addOnSuccessListener(r -> onResult.accept(null))
                .addOnFailureListener(e -> onResult.accept(new ServiceException(R.string.error_cant_update_community, e)));
    }

    @Override
    public boolean isCommunityUpdateRequestInvalid(@NonNull CommunityUpdateRequest request) {
        if (request.getAvatar() != null && AvatarManager.isAvatarInvalid(request.getAvatar()))
            return true;

        String name = request.getName();
        String description = request.getDescription();

        if ((request.isNameSet() && name == null) || (request.isDescriptionSet() && description == null))
            return true;

        if (request.isNameSet()) {
            request.setName(name = name.trim());
            if (name.isEmpty()) return true;
        }

        if (request.isDescriptionSet()) {
            request.setDescription(description = description.trim());
            return description.isEmpty();
        }

        return false;
    }

    @Override
    public void getCommunity(@NonNull String cid, @NonNull Consumer<Community> onSuccess, @NonNull Consumer<ServiceException> onFailure) {
        communityCollection.document(cid).get()
                .addOnSuccessListener(snapshot ->
                        onSuccess.accept(Objects.requireNonNull(communityFromFirestoreCommunity(cid, snapshot.toObject(FirestoreCommunity.class))))
                )
                .addOnFailureListener(e -> onFailure.accept(new ServiceException(R.string.error_cant_load_community, e)));
    }

    @Override
    public void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<Community, ServiceException> listener) {
        ListenerRegistration listenerRegistration = communityCollection.document(cid).addSnapshotListener(
                (snapshot, e) -> {
                    if (e != null || !Objects.requireNonNull(snapshot).exists()) {
                        listener.accept(null, new ServiceException(R.string.error_cant_load_community, e));
                        return;
                    }

                    listener.accept(communityFromFirestoreCommunity(cid, snapshot.toObject(FirestoreCommunity.class)), null);
                }
        );

        FirestoreUtils.attachObserver(listenerRegistration, lifecycleOwner);
    }

}

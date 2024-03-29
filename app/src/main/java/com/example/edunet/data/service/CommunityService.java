package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.util.common.Paginator;

import java.util.function.BiConsumer;

public interface CommunityService {
    /**
     * please call this only in persistent operations
     *
     * @param request  the request
     * @param onResult callback that will be called on result
     */
    void createCommunity(@NonNull CommunityCreateRequest request, @NonNull Consumer<ServiceException> onResult);

    Paginator<Pair<String, Community>> getCommunityPaginator(String namePrefix, int limit);

    void deleteCommunity(@NonNull String id, Consumer<ServiceException> onResult);

    void requestAdminPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult);

    void requestParticipantPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult);


    void setAdminPermissions(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void deleteAdminRequest(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void deleteAdmin(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void deleteParticipant(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void setParticipantPermissions(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void deleteParticipantRequest(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    boolean validateCommunityCreateRequest(@NonNull CommunityCreateRequest request);

    void updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean validateCommunityUpdateRequest(@NonNull CommunityUpdateRequest request);

    void getCommunity(@NonNull String cid, @NonNull Consumer<Community> onSuccess, @NonNull Consumer<ServiceException> onFailure);

    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<Community, ServiceException> listener);

    void observeAttachedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Pair<String, Community>[]> biConsumer);

    void observeOwnedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Pair<String, Community>[]> biConsumer);

    void observeAdminedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Pair<String, Community>[]> biConsumer);

    void observeParticipatedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Pair<String, Community>[]> biConsumer);

    void observeSubCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<ServiceException, Pair<String, Community>[]> biConsumer);

}

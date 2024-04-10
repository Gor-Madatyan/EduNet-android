package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.util.paginator.Paginator;

import java.util.function.BiConsumer;

public interface CommunityService {
    /**
     * please call this only in persistent operations
     *
     * @param request  the request
     * @param onResult callback that will be called on result
     */
    void createCommunity(@NonNull CommunityCreateRequest request, @NonNull Consumer<ServiceException> onResult);

    Paginator<Community> getCommunityPaginator(String namePrefix, int limit);

    void deleteCommunity(@NonNull String id, Consumer<ServiceException> onResult);

    void requestAdminPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult);

    void requestParticipantPermissions(@NonNull String cid, @NonNull Consumer<ServiceException> onResult);

    void deleteAdmin(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void deleteParticipant(@NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    void graduateParticipants(@NonNull String cid, @NonNull String[] uids, @NonNull Consumer<ServiceException> onResult);

    void managePermissions(@NonNull Role role, boolean accept, @NonNull String cid, @NonNull String uid, @NonNull Consumer<ServiceException> onResult);

    boolean isCommunityCreateRequestInvalid(@NonNull CommunityCreateRequest request);

    void updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean isCommunityUpdateRequestInvalid(@NonNull CommunityUpdateRequest request);

    void getCommunity(@NonNull String cid, @NonNull Consumer<Community> onSuccess, @NonNull Consumer<ServiceException> onFailure);

    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<Community, ServiceException> listener);

    void observeAttachedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

    void observeOwnedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

    void observeAdminedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

    void observeParticipatedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

    void observeGraduatedCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String uid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

    void observeSubCommunities(@NonNull LifecycleOwner lifecycleOwner, @NonNull String cid, @NonNull BiConsumer<ServiceException, Community[]> biConsumer);

}

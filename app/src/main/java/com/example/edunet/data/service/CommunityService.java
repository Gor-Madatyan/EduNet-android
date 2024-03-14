package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityCreateRequest;
import com.example.edunet.data.service.model.CommunityUpdateRequest;

import java.util.List;
import java.util.function.BiConsumer;

public interface CommunityService {

    /**
     * please call this only in persistent operations
     *
     * @param request  the request
     * @param onResult callback that will be called on result
     */
    void createCommunity(@NonNull CommunityCreateRequest request, @NonNull Consumer<ServiceException> onResult);

    void deleteCommunity(@NonNull String id, Consumer<ServiceException> onResult);

    boolean validateCommunityCreateRequest(@NonNull CommunityCreateRequest request);

    void updateCommunity(@NonNull CommunityUpdateRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean validateCommunityUpdateRequest(@NonNull CommunityUpdateRequest request);

    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id, @NonNull BiConsumer<Community, ServiceException> listener);

    void loadCommunities(@NonNull List<String> communityIds, @NonNull Consumer<List<Pair<String, Community>>> onSuccess, @NonNull Consumer<ServiceException> onFailure);
}

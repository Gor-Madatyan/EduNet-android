package com.example.edunet.data.service;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;

import com.example.edunet.data.service.exception.ServiceException;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.model.CommunityModifyRequest;

import java.util.function.BiConsumer;

public interface CommunityService {

    /**
     * please call this only in persistent operations
     *
     * @param request  the request
     * @param onResult callback that will be called on result
     */
    void createCommunity(@NonNull CommunityModifyRequest request, @NonNull Consumer<ServiceException> onResult);

    boolean validateCommunityCreateRequest(@NonNull CommunityModifyRequest request);

    void observeCommunity(@NonNull LifecycleOwner lifecycleOwner, @NonNull String id, @NonNull BiConsumer<Community, ServiceException> listener);

}

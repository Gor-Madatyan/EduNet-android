package com.example.edunet.data.service.api;

import androidx.annotation.NonNull;

public interface StorageService {

    /**
     * If this method returns {@code true} it means that
     * provided url refers to a file in this storage.
     * @param url URL to check
     * @return true if provided url is domestic
     */
    boolean isUrlDomestic(@NonNull String url);

}

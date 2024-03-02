package com.example.edunet.data.service.impl;

import androidx.annotation.NonNull;

import com.example.edunet.data.service.api.StorageService;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Inject;

public final class StorageServiceImpl implements StorageService {
    private final FirebaseStorage storage;

    @Inject
     StorageServiceImpl(FirebaseStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean isUrlDomestic(@NonNull String url) {
        try {
            storage.getReferenceFromUrl(url);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

}

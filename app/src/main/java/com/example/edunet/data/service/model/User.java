package com.example.edunet.data.service.model;

import android.net.Uri;

import androidx.core.util.Pair;


public record User(String id, String name, Uri photo, String bio, Pair<String,Community>[] ownedCommunities) {
}

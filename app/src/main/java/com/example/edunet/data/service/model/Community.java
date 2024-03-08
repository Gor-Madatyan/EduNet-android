package com.example.edunet.data.service.model;

import androidx.annotation.NonNull;

public class Community {
    private String name;
    private String avatar;
    private String description;
    private String ownerId;

    public enum Role{
        OWNER
    }

    public Community() {
    }

    public Community(String name, String description, String avatar, String ownerId) {
        this.name = name;
        this.avatar = avatar;
        this.description = description;
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Community{" +
                "name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", description='" + description + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getOwnerId() {
        return ownerId;
    }
}

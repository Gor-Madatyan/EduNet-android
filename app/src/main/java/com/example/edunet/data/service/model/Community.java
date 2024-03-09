package com.example.edunet.data.service.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Community implements Parcelable {
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

    protected Community(Parcel in) {
        name = in.readString();
        avatar = in.readString();
        description = in.readString();
        ownerId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(description);
        dest.writeString(ownerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Community> CREATOR = new Creator<>() {
        @Override
        public Community createFromParcel(Parcel in) {
            return new Community(in);
        }

        @Override
        public Community[] newArray(int size) {
            return new Community[size];
        }
    };

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

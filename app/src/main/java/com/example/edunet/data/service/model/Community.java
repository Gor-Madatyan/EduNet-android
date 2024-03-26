package com.example.edunet.data.service.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edunet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


// FIXME: 3/26/2024 Community is already not POJO and I want to
//  store in it not only simple types such as String and also uris,
//  build abstraction over Community class, and make Community class simple POJO
@SuppressWarnings("unused")
public class Community implements Entity, Parcelable {
    private String name;
    private String searchName;
    private String avatar;
    private String description;
    private String ancestor;
    private List<String> admins;
    private List<String> adminsQueue;
    private List<String> participants;
    private List<String> participantsQueue;
    private String ownerId;

    public Community() {
    }

    public Community(@NonNull String name, @NonNull String description, @Nullable String avatar, @Nullable String ancestor, @NonNull String ownerId) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @DrawableRes
    @Override
    public int requireDefaultAvatar() {
        return R.drawable.ic_default_group;
    }

    @SuppressWarnings("unused")
    public String getSearchName() {
        return searchName;
    }


    public String getOwnerId() {
        return ownerId;
    }

    @SuppressWarnings("unused")
    public String getAncestor() {
        return ancestor;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public List<String> getAdminsQueue() {
        return adminsQueue;
    }

    public List<String> getParticipantsQueue() {
        return participantsQueue;
    }
}

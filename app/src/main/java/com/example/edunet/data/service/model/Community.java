package com.example.edunet.data.service.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edunet.R;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Community extends Entity implements Parcelable {
    private final String name;
    private final Uri avatar;
    private final String description;
    private final List<String> admins;
    private final List<String> adminsQueue;
    private final List<String> participants;
    private final List<String> participantsQueue;
    private final List<String> graduated;
    private final Map<String, List<String>> graduations;
    private final String id;
    private final String ancestor;
    private final String ownerId;

    public Role getUserRole(String uid) {
        return uid.equals(getOwnerId()) ? Role.OWNER :
                getAdmins().contains(uid) ? Role.ADMIN :
                        getParticipants().contains(uid) ? Role.PARTICIPANT :
                                getGraduated().contains(uid) ? Role.GRADUATED :
                                        Role.GUEST;
    }

    public Community(@NonNull String name,
                     @NonNull String description,
                     @Nullable Uri avatar,
                     @NonNull List<String> admins,
                     @NonNull List<String> adminsQueue,
                     @NonNull List<String> participants,
                     @NonNull List<String> participantsQueue,
                     @NonNull List<String> graduated,
                     @NonNull Map<String, List<String>> graduations,
                     @Nullable String ancestor,
                     @NonNull String id,
                     @NonNull String ownerId) {
        this.name = name;
        this.avatar = avatar;
        this.description = description;
        this.admins = admins;
        this.adminsQueue = adminsQueue;
        this.participants = participants;
        this.participantsQueue = participantsQueue;
        this.graduated = graduated;
        this.graduations = graduations;
        this.ancestor = ancestor;
        this.ownerId = ownerId;
        this.id = id;

    }

    @SuppressWarnings("unchecked")
    protected Community(Parcel in) {
        name = in.readString();
        avatar = in.readParcelable(Uri.class.getClassLoader());
        description = in.readString();
        admins = in.createStringArrayList();
        adminsQueue = in.createStringArrayList();
        participants = in.createStringArrayList();
        participantsQueue = in.createStringArrayList();
        graduated = in.createStringArrayList();
        graduations = (Map<String, List<String>>) in.readSerializable();
        id = in.readString();
        ancestor = in.readString();
        ownerId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(avatar, flags);
        dest.writeString(description);
        dest.writeStringList(admins);
        dest.writeStringList(adminsQueue);
        dest.writeStringList(participants);
        dest.writeStringList(participantsQueue);
        dest.writeStringList(graduated);
        dest.writeSerializable((Serializable) graduations);
        dest.writeString(id);
        dest.writeString(ancestor);
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

    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Uri getAvatar() {
        return avatar;
    }

    @DrawableRes
    @Override
    public int requireDefaultAvatar() {
        return R.drawable.ic_default_group;
    }

    @SuppressWarnings("unused")

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

    public List<String> getGraduated() {
        return graduated;
    }

    public Map<String, List<String>> getGraduations() {
        return graduations;
    }
}

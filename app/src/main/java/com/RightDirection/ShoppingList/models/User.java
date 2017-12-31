package com.RightDirection.ShoppingList.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    @SuppressWarnings("WeakerAccess")
    public String name;
    @SuppressWarnings("WeakerAccess")
    public String photoUrl;
    @SuppressWarnings("unused, WeakerAccess")
    public String userEmail;
    private String uid;

    public User(){}

    public User(String uid, String name, String photoUrl) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.uid = uid;
    }

    protected User(Parcel in) {
        name = in.readString();
        photoUrl = in.readString();
        userEmail = in.readString();
        uid = in.readString();
    }

    @SuppressWarnings("unused")
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(photoUrl);
        dest.writeString(userEmail);
        dest.writeString(uid);
    }
}

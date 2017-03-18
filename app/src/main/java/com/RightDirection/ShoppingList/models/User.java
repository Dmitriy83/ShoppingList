package com.RightDirection.ShoppingList.models;

public class User {

    @SuppressWarnings("WeakerAccess")
    public String name;
    @SuppressWarnings("WeakerAccess")
    public String photoUrl;
    @SuppressWarnings("unused|WeakerAccess")
    public String userEmail;
    private String uid;

    public User(){}

    public User(String uid, String name, String photoUrl) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.uid = uid;
    }

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
}

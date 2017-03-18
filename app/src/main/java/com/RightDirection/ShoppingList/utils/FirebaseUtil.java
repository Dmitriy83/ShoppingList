package com.RightDirection.ShoppingList.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.RightDirection.ShoppingList.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    private static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("users").child(getCurrentUserId());
        }
        return null;
    }

    public static String getShoppingListsPath() {
        return "shopping_lists/";
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static String getUsersPath() {
        return "users/";
    }

    public static void writeUserToPref(Context context, User user){
        SharedPreferences settings = context.getSharedPreferences("AUTH_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("AuthorUID", user.getUid());
        editor.putString("AuthorFullName", user.getName());
        editor.putString("AuthorProfilePicture", user.getPhotoUrl());
        editor.apply();
    }

    public static void removeUserFromPref(Context context){
        SharedPreferences settings = context.getSharedPreferences("AUTH_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("AuthorUID", null);
        editor.putString("AuthorFullName", null);
        editor.putString("AuthorProfilePicture", null);
        editor.apply();
    }

    public static User readUserFromPref(Context context){
        SharedPreferences settings = context.getSharedPreferences("AUTH_SETTINGS", Context.MODE_PRIVATE);
        String id = settings.getString("AuthorUID", null);
        String name = settings.getString("AuthorFullName", "");
        String picture = settings.getString("AuthorProfilePicture", null);
        if (id != null){
            return new User(id, name, picture);
        } else{
            return null;
        }
    }

    public static DatabaseReference getFriendsRef() {
        return FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).child("friends");
    }

    public static String getEmailKey(){return "userEmail";}
}

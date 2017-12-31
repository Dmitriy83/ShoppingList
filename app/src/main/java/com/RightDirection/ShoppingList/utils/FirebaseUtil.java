package com.RightDirection.ShoppingList.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.services.ExchangeService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtil {

    static final String SHOPPING_LISTS_PATH = "shopping_lists/";
    private static final String BLACK_LIST_PATH = "black_list/";
    public static final String EMAIL_KEY = "userEmail";
    private static final String FRIENDS_PATH = "friends/";
    private static final String USERS_PATH = "users/";

    static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    private static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    private static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child(USERS_PATH).child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child(USERS_PATH);
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
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef == null) return null;
        return currentUserRef.child(FRIENDS_PATH);
    }

    static DatabaseReference getShoppingListsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef == null) return null;
        return currentUserRef.child(FirebaseUtil.SHOPPING_LISTS_PATH);
    }

    static ArrayList<FirebaseShoppingList> getShoppingListsFromFB(DataSnapshot dataSnapshot){
        ArrayList<FirebaseShoppingList> firebaseLists = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            FirebaseShoppingList firebaseShoppingList = childDataSnapshot.getValue(FirebaseShoppingList.class);
            if (firebaseShoppingList != null) {
                firebaseShoppingList.setId(childDataSnapshot.getKey());
                firebaseLists.add(firebaseShoppingList);
            }
        }

        return firebaseLists;
    }

    public static void removeCurrentUserShoppingListsFromFirebase(ArrayList<FirebaseShoppingList> shoppingListsForDelete) {
        DatabaseReference shoppingListsRef = FirebaseUtil.getShoppingListsRef();
        if (shoppingListsRef == null) return;

        for (FirebaseShoppingList fbList: shoppingListsForDelete) {
            DatabaseReference fbListRef = shoppingListsRef.child(fbList.getId());
            if (fbListRef != null){
                fbListRef.removeValue();
            }
        }
    }

    public static boolean userSignedIn(Context context) {
        User user = readUserFromPref(context);
        return (user != null);
    }

    public static DatabaseReference getBlackListRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef == null) return null;
        return currentUserRef.child(BLACK_LIST_PATH);
    }

    public static void restartServiceToReceiveShoppingListsFromFirebase(Context context){
        if (context == null) return;

        Toast.makeText(context, R.string.receiving, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, ExchangeService.class);
        // Т.к. пользователь запустил команду интерактивно, будем оповещать его о таймаутах, ошибках соединения и т.д.
        intent.putExtra(EXTRAS_KEYS.NOTIFY_SOURCE_ACTIVITY.getValue(), true);
        // Если сервис был запущен по таймеру, остановим его, чтобы пользователю
        // передавались сообщения (по таймеру сообщения не возвращаются).
        context.stopService(intent);
        context.startService(intent);
    }
}

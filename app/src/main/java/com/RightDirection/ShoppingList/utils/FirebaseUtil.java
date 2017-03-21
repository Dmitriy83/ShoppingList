package com.RightDirection.ShoppingList.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FirebaseUtil {

    public static final String SHOPPING_LISTS_PATH = "shopping_lists/";
    public static final String EMAIL_KEY = "userEmail";

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

    private static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("users").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
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
        return currentUserRef.child("friends");
    }

    public static DatabaseReference getShoppingListsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef == null) return null;
        return currentUserRef.child(FirebaseUtil.SHOPPING_LISTS_PATH);
    }

    public static ArrayList<ShoppingList> loadShoppingLists(Context context, DataSnapshot dataSnapshot){

        ArrayList<FirebaseShoppingList> firebaseLists = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            FirebaseShoppingList firebaseShoppingList = childDataSnapshot.getValue(FirebaseShoppingList.class);
            firebaseShoppingList.setName(childDataSnapshot.getKey());
            firebaseLists.add(firebaseShoppingList);
        }

        // Загружаем новый списков покупок
        ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();
        for (FirebaseShoppingList firebaseList: firebaseLists) {
            // Сформируем имя нового списка покупок
            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            String newListName = firebaseList.getName() + " "
                    + context.getString(R.string.loaded) + " "
                    + dateFormat.format(calendar.getTime());

            // Создадим  новый объект-лист покупок
            ShoppingList newShoppingList = new ShoppingList(-1, newListName);
            newShoppingList.loadProductsFromString(context, firebaseList.getContent());
            newShoppingList.addNotExistingProductsToDB(context);
            // Сначала нужно добавить новые продукты из списка в базу данных.
            // Синхронизацияя должна производиться по полю Name
            newShoppingList.addNotExistingProductsToDB(context);
            // Сохраним новый лист покупок в базе данных
            newShoppingList.addToDB(context);
            loadedShoppingLists.add(newShoppingList);
        }

        return loadedShoppingLists;
    }

    public static void removeCurrentUserShoppingListsFromFirebase() {
        DatabaseReference shoppingListsRef = FirebaseUtil.getShoppingListsRef();
        if (shoppingListsRef != null) shoppingListsRef.removeValue();
    }

    public static boolean userSignedIn(Context context) {
        User user = readUserFromPref(context);
        return (user != null);
    }
}

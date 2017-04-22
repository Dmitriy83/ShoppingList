package com.RightDirection.ShoppingList.models;

import java.util.ArrayList;

/**
 * POJO для данных пользователя, полученных из FireBase
 */
public class UserData extends ArrayList<ShoppingList> {
    private final ArrayList<FirebaseShoppingList> shoppingLists;
    private final ArrayList<User> friends;
    private final ArrayList<User> blackList;

    public UserData(ArrayList<FirebaseShoppingList> shoppingLists, ArrayList<User> friends, ArrayList<User> blackList) {
        this.shoppingLists = shoppingLists;
        this.friends = friends;
        this.blackList = blackList;
    }

    public ArrayList<FirebaseShoppingList> getShoppingLists() {
        return shoppingLists;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public ArrayList<User> getBlackList() {
        return blackList;
    }
}

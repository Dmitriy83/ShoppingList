package com.RightDirection.ShoppingList.models;

public class FirebaseShoppingList {
    @SuppressWarnings("WeakerAccess")
    public String content;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String getContent(){
        return this.content;
    }
}

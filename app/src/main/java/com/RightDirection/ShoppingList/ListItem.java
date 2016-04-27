package com.RightDirection.ShoppingList;

public class ListItem {

    private String id;
    private String name;

    public ListItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

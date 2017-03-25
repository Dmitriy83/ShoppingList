package com.RightDirection.ShoppingList.models;

public class FirebaseShoppingList {
    @SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
    public String content;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
    public String authorId;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
    public String name;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
    public User author;
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getContent(){
        return this.content;
    }

    public User getAuthor() {
        return author;
    }

    public String getId() {
        return id;
    }
}

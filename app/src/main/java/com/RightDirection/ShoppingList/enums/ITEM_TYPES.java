package com.RightDirection.ShoppingList.enums;

public enum ITEM_TYPES {
    PRODUCT (0),
    CATEGORY (1);

    private int numValue;

    ITEM_TYPES(int numValue) {
        this.numValue = numValue;
    }

    public int getNumValue() {
        return numValue;
    }
}

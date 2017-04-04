package com.RightDirection.ShoppingList.enums;

import com.RightDirection.ShoppingList.interfaces.IGetValue;

public enum EXTRAS_KEYS implements IGetValue {
    SHOPPING_LIST ("Shopping list"),
    PRODUCT ("Product"),
    PRODUCTS("Products"),
    PRODUCTS_NAMES("Products names"),
    PRODUCTS_ORIGINAL_VALUES("Products - original values"),
    ITEM_IMAGE("Item image"),
    IS_FILTERED("Is filtered"),
    NOTIFICATION("NOTIFICATION"),
    NOTIFY_SOURCE_ACTIVITY("NOTIFY_SOURCE_ACTIVITY"),
    SHOPPING_LISTS("Shopping lists"),
    AUTHOR("AUTHOR"),
    CATEGORY ("Category");

    private final String numValue;

    EXTRAS_KEYS(String numValue) {
        this.numValue = numValue;
    }

    @Override
    public String getValue() {
        return numValue;
    }
}

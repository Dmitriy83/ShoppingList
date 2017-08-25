package com.RightDirection.ShoppingList.enums;

import com.RightDirection.ShoppingList.interfaces.IGetValue;

public enum ITEM_TYPES implements IGetValue {
    PRODUCT (0),
    CATEGORY (1);

    private final Integer numValue;

    ITEM_TYPES(int numValue) {
        this.numValue = numValue;
    }

    @Override
    public Integer getValue() {
        return numValue;
    }
}

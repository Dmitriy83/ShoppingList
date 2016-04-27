package com.RightDirection.ShoppingList.interfaces;

import android.support.annotation.Nullable;
import com.RightDirection.ShoppingList.ListItem;

public interface IOnDeleteItemListener {
    void onDeleteItem(@Nullable ListItem item);
}

package com.RightDirection.ShoppingList.interfaces;

import android.content.Context;

public interface IDataBaseOperations {
    void addToDB(Context context);

    void removeFromDB(Context context);

    void updateInDB(Context context);

    void renameInDB(Context context);
}

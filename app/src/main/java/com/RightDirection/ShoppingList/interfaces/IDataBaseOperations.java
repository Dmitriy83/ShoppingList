package com.RightDirection.ShoppingList.interfaces;

import android.content.Context;

public interface IDataBaseOperations {
    void addToDB(Context context);

    @SuppressWarnings("unused")
    void removeFromDB(Context context);

    @SuppressWarnings("unused")
    void updateInDB(Context context);

    @SuppressWarnings("unused")
    void renameInDB(Context context);
}

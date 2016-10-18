package com.RightDirection.ShoppingList.interfaces;

import android.content.Context;

public interface IDataBaseOperations {
    public void addToDB(Context context);

    public void removeFromDB(Context context);

    public void updateInDB(Context context);

    public void renameInDB(Context context);
}

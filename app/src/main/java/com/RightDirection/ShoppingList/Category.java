package com.RightDirection.ShoppingList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;

public class Category extends ListItem implements IDataBaseOperations {
    public Category(long id, String name) {
        super(id, name);
    }

    protected Category(Parcel in) {
        super(in);
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        Uri insertedId = contentResolver.insert(ShoppingListContentProvider.CATEGORIES_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));
    }

    @Override
    public void removeFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ShoppingListContentProvider.CATEGORIES_CONTENT_URI,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        contentResolver.update(ShoppingListContentProvider.CATEGORIES_CONTENT_URI, contentValues,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void renameInDB(Context context) {
        // Для категория функция идентична
        updateInDB(context);
    }
}

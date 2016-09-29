package com.RightDirection.ShoppingList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;

public class Product extends ListItem implements IDataBaseOperations {

    public Product(long id, String name, Uri imageUri) {
        super(id, name, imageUri);
    }

    public Product(long id, String name, Uri imageUri, float count) {
        super(id, name, imageUri, count);
    }

    protected Product(Parcel in) {
        super(in);
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        if (getImageUri() != null) {
            contentValues.put(ShoppingListContentProvider.KEY_PICTURE, getImageUri().toString());
        }
        Uri insertedId = contentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        if (getImageUri() != null) {
            contentValues.put(ShoppingListContentProvider.KEY_PICTURE, getImageUri().toString());
        }
        contentResolver.update(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }
}

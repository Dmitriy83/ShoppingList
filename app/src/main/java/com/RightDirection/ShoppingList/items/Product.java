package com.RightDirection.ShoppingList.items;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.ShoppingListContentProvider;

public class Product extends ListItem implements IDataBaseOperations {

    private Category category;

    public Product(long id, String name) {
        super(id, name);
    }

    public Product(long id, String name, Uri imageUri) {
        super(id, name, imageUri);
    }

    public Product(long id, String name, Uri imageUri, float count) {
        super(id, name, imageUri, count);
    }

    public Product(long id, String name, Uri imageUri, float count, Category category) {
        super(id, name, imageUri, count);

        this.category = category;
    }

    protected Product(Parcel in) {
        super(in);
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        if (category != null)
            contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_ID, category.getId());
        if (getImageUri() != null)
            contentValues.put(ShoppingListContentProvider.KEY_PICTURE, getImageUri().toString());
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
        if (category != null)
            contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_ID, category.getId());
        if (getImageUri() != null)
            contentValues.put(ShoppingListContentProvider.KEY_PICTURE, getImageUri().toString());
        contentResolver.update(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ShoppingListContentProvider.KEY_NAME, getName());
        contentResolver.update(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                values, ShoppingListContentProvider.KEY_ID +  " = " + getId(), null);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public ITEM_TYPES getType(){
        return ITEM_TYPES.PRODUCT;
    }
}

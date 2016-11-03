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

public class Category extends ListItem implements IDataBaseOperations {

    private int order;

    public Category(long id, String name, int order) {
        super(id, name);
        this.order = order;
    }

    protected Category(Parcel in) {
        super(in);
        order = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeParcelable(imageUri, flags);
        dest.writeFloat(count);
        dest.writeInt(order);
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_NAME, getName());
        contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_ORDER, getOrder());
        Uri insertedId = contentResolver.insert(ShoppingListContentProvider.CATEGORIES_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));
    }

    @Override
    public void removeFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ShoppingListContentProvider.CATEGORIES_CONTENT_URI,
                ShoppingListContentProvider.KEY_CATEGORY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_NAME, getName());
        contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_ORDER, getOrder());
        contentResolver.update(ShoppingListContentProvider.CATEGORIES_CONTENT_URI, contentValues,
                ShoppingListContentProvider.KEY_CATEGORY_ID + "=" + getId(), null);
    }

    @Override
    public void renameInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_CATEGORY_NAME, getName());
        contentResolver.update(ShoppingListContentProvider.CATEGORIES_CONTENT_URI, contentValues,
                ShoppingListContentProvider.KEY_CATEGORY_ID + "=" + getId(), null);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public ITEM_TYPES getType() {
        return ITEM_TYPES.CATEGORY;
    }
}

package com.RightDirection.ShoppingList.models;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.activities.CategoryActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

public class Category extends ListItem implements IDataBaseOperations {

    private int order;

    public Category(long id, String name, int order) {
        super(id, name);
        this.order = order;
    }

    public Category(Cursor data){
        super(data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_CATEGORY_ID)),
                data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_CATEGORY_NAME)),
                SL_ContentProvider.getCategoryImageUri(data));
        this.order = data.getInt(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_CATEGORY_ORDER));
    }

    private Category(Parcel in) {
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
        super.writeToParcel(dest, flags);
        dest.writeInt(order);
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_NAME, getName());
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_ORDER, getOrder());
        if (getImageUri() != null) {
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_PICTURE_URI, getImageUri().toString());
        }else{
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_PICTURE_URI, (byte[]) null);
        }
        Uri insertedId = contentResolver.insert(SL_ContentProvider.CATEGORIES_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));

        // Категория более не новая
        isNew = false;
    }

    @Override
    public void removeFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(SL_ContentProvider.CATEGORIES_CONTENT_URI,
                SL_ContentProvider.KEY_CATEGORY_ID + "= ?", new String[]{String.valueOf(getId())});
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_NAME, getName());
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_ORDER, getOrder());
        if (getImageUri() != null) {
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_PICTURE_URI, getImageUri().toString());
        }else{
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_PICTURE_URI, (byte[]) null);
        }
        contentResolver.update(SL_ContentProvider.CATEGORIES_CONTENT_URI, contentValues,
                SL_ContentProvider.KEY_CATEGORY_ID + "= ?", new String[]{String.valueOf(getId())});
    }

    @Override
    public void renameInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_NAME, getName());
        contentResolver.update(SL_ContentProvider.CATEGORIES_CONTENT_URI, contentValues,
                SL_ContentProvider.KEY_CATEGORY_ID + "= ?", new String[]{String.valueOf(getId())});
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

    public void startCategoryActivity(Activity activity){
        Intent intent = new Intent(activity, CategoryActivity.class);
        intent.putExtra(EXTRAS_KEYS.CATEGORY.getValue(), this);
        activity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
    }
}

package com.RightDirection.ShoppingList.items;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.contentProvider;

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

    public Product(long id, String name, Uri imageUri, float count, boolean isChecked) {
        super(id, name, imageUri, count);

        this.isChecked = isChecked;
    }

    public Product(Cursor data, Category category){
        super(data.getLong(data.getColumnIndexOrThrow(contentProvider.KEY_ID)),
                data.getString(data.getColumnIndexOrThrow(contentProvider.KEY_NAME)),
                contentProvider.getImageUri(data));

        try {
            int countColumnIndex = data.getColumnIndexOrThrow(contentProvider.KEY_COUNT);
            int countIsCheckedIndex = data.getColumnIndexOrThrow(contentProvider.KEY_IS_CHECKED);
            this.count = data.getFloat(countColumnIndex);
            this.isChecked = data.getInt(countIsCheckedIndex) != 0;
        } catch (Exception e){
            // Столбец не найден
            this.count = 1;
            this.isChecked = false;
        }

        this.category = category;
    }

    protected Product(Parcel in) {
        super(in);
        category = in.readParcelable(Category.class.getClassLoader());
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(category, flags);
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        // Если товар с данным именем уже есть в БД, то создавать новый не нужно.
        // Необходимо присвоить id найденного элемента текущему.
        Cursor data = contentResolver.query(contentProvider.PRODUCTS_CONTENT_URI, null,
                contentProvider.KEY_NAME + " = '" + getName() + "'", null, null);
        if (data != null){
            if (data.moveToNext()) {
                int keyIdIndex = data.getColumnIndexOrThrow(contentProvider.KEY_ID);
                setId(data.getLong(keyIdIndex));
                Toast.makeText(context, context.getString(R.string.product_is_exist),
                        Toast.LENGTH_SHORT).show();
                data.close();
                return;
            }
            data.close();

            // Продукт более не новый
            isNew = false;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(contentProvider.KEY_NAME, getName());
        if (category != null)
            contentValues.put(contentProvider.KEY_CATEGORY_ID, category.getId());
        if (getImageUri() != null)
            contentValues.put(contentProvider.KEY_PICTURE, getImageUri().toString());
        Uri insertedId = contentResolver.insert(contentProvider.PRODUCTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(contentProvider.PRODUCTS_CONTENT_URI,
                contentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(contentProvider.KEY_NAME, getName());
        if (category != null)
            contentValues.put(contentProvider.KEY_CATEGORY_ID, category.getId());
        if (getImageUri() != null)
            contentValues.put(contentProvider.KEY_PICTURE, getImageUri().toString());
        contentResolver.update(contentProvider.PRODUCTS_CONTENT_URI, contentValues,
                contentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(contentProvider.KEY_NAME, getName());
        contentResolver.update(contentProvider.PRODUCTS_CONTENT_URI,
                values, contentProvider.KEY_ID +  " = " + getId(), null);
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

package com.RightDirection.ShoppingList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;

import java.util.ArrayList;

public class ShoppingList extends ListItem implements IDataBaseOperations {

    ArrayList<Product> mProducts;

    public ShoppingList(long id, String name, Uri imageUri) {
        super(id, name, imageUri);
    }

    public ShoppingList(long id, String name, Uri imageUri, float count) {
        super(id, name, imageUri, count);
    }

    public ShoppingList(long id, String name, Uri imageUri,  ArrayList<Product> products) {
        super(id, name, imageUri);
        mProducts = products;
    }

    public ShoppingList(long id, String name, Uri imageUri, float count,  ArrayList<Product> products) {
        super(id, name, imageUri, count);
        mProducts = products;
    }

    protected ShoppingList(Parcel in) {
        super(in);
    }

    public void addProduct(Product product){
        mProducts.add(product);
    }

    public ArrayList<Product> getProducts(){
        return mProducts;
    }

    public void setProducts(ArrayList<Product> products){
        mProducts = products;
    }

    @Override
    public void addToDB(Context context) {
        // Сохраним список продуктов в БД
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Заполним значения для сохранения в базе данных
        // и запишем новый список покупок в таблицу SHOPPING_LISTS
        contentValues.put(ShoppingListContentProvider.KEY_NAME, getName());
        Uri insertedId = contentResolver.insert(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));

        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

        // Запишем составлящие списка покупок в базу данных
        for (Product item : mProducts) {
            contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(ShoppingListContentProvider.KEY_COUNT, item.getCount());
            contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Сначала удалим все записи редактируемого списка покупок из БД
        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null);

        // Запишем составлящие списка покупок в базу данных
        for (ListItem item: mProducts) {
            contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(ShoppingListContentProvider.KEY_COUNT, item.getCount());
            contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }
    }

    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ShoppingListContentProvider.KEY_NAME, getName());
        contentResolver.update(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, ShoppingListContentProvider.KEY_ID +  " = " + getId(), null);
    }

    public void addNotExistingProductsToDB(Context context) {
        // Создадим строку условия
        String where = getWhereConditionForName();

        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        ArrayList<String> foundProducts = new ArrayList<>();
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        while (data.moveToNext()){
            foundProducts.add(data.getString(keyNameIndex));
        }
        data.close();

        // Добавим несуществующие продукты в базу данных
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < mProducts.size(); i++) {
            String name = mProducts.get(i).getName();
            if (!foundProducts.contains(name)){
                contentValues.put(ShoppingListContentProvider.KEY_NAME, name);
                contentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
            }
        }
    }

    public void setProductsIdFromDB(Context context) {
        // Создадим строку условия
        String where = getWhereConditionForName();
        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);
        int i = 0;
        while (data.moveToNext()){
            mProducts.get(i).setId(data.getLong(keyIdIndex));
            i++;
        }
        data.close();
    }

    private String getWhereConditionForName() {
        String where = null;
        if (mProducts.size() > 0) {
            where = ShoppingListContentProvider.KEY_NAME + " IN (";
            where += "'" + mProducts.get(0).getName() + "'";
            for (int i = 1; i < mProducts.size(); i++) {
                where += ",'" + mProducts.get(i).getName() + "'";
            }
            where += ")";
        }
        return where;
    }
}

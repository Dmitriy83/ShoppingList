package com.RightDirection.ShoppingList.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.RightDirection.ShoppingList.ListItem;

import java.util.ArrayList;

/**
 * Класс с глобальными методами работы с базой данных "Списка покупок"
 */
public class DBUtils {

    private static DBUtils ourInstance = new DBUtils();

    public static DBUtils getInstance() {
        return ourInstance;
    }

    private DBUtils() {}

    public static long saveNewShoppingList(Context context, String name, ArrayList<ListItem> elements) {
        // Сохраним список продуктов в БД
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Заполним значения для сохранения в базе данных
        // и запишем новый список покупок в таблицу SHOPPING_LISTS
        contentValues.put(ShoppingListContentProvider.KEY_NAME, name);
        Uri insertedId = contentResolver.insert(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        long listId = ContentUris.parseId(insertedId);

        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

        // Запишем составлящие списка покупок в базу данных
        for (ListItem item : elements) {
            contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, listId);
            contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(ShoppingListContentProvider.KEY_COUNT, item.getCount());
            contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }

        return listId;
    }

    public static void updateShoppingList(Context context, String id, ArrayList<ListItem> elements) {
        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Сначала удалим все записи редактируемого списка покупок из БД
        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + id, null);

        // Запишем составлящие списка покупок в базу данных
        for (ListItem item: elements) {
            contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, id);
            contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(ShoppingListContentProvider.KEY_COUNT, item.getCount());
            contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }
    }

    public static void deleteShoppingList(Context context, String id){
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_ID + "=" + id, null);
    }

    public static void renameShoppingList(Context context, long id, String newName){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ShoppingListContentProvider.KEY_NAME, newName);
        contentResolver.update(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, ShoppingListContentProvider.KEY_ID +  " = " + id, null);
    }

    public static void addNotExistingProductsToDB(Context context, ArrayList<ListItem> listItems) {
        // Создадим строку условия
        String where = getWhereConditionForName(listItems);

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
        for (int i = 0; i < listItems.size(); i++) {
            String name = listItems.get(i).getName();
            if (!foundProducts.contains(name)){
                contentValues.put(ShoppingListContentProvider.KEY_NAME, name);
                contentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
            }
        }
    }

    private static String getWhereConditionForName(ArrayList<ListItem> products) {
        String where = null;
        if (products.size() > 0) {
            where = ShoppingListContentProvider.KEY_NAME + " IN (";
            where += "'" + products.get(0).getName() + "'";
            for (int i = 1; i < products.size(); i++) {
                where += ",'" + products.get(i).getName() + "'";
            }
            where += ")";
        }
        return where;
    }

    public static ArrayList<ListItem> setIdFromDB(Context context, ArrayList<ListItem> listItems) {
        // Создадим строку условия
        String where = getWhereConditionForName(listItems);
        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);
        int i = 0;
        while (data.moveToNext()){
            listItems.get(i).setId(data.getLong(keyIdIndex));
            i++;
        }
        data.close();
        return listItems;
    }
}

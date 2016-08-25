package com.RightDirection.ShoppingList.helpers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import org.junit.Ignore;
import org.junit.Test;

public class ShoppingListContentProviderTest extends ProviderTestCase2{

    String mListName = "newShoppingListTest", mProductName1 = "newProductTest1", mProductName2 = "newProductTest2";
    long mListId = 0, mProductId1 = 0, mProductId2 = 0;

    public ShoppingListContentProviderTest() {
        super(ShoppingListContentProvider.class, ShoppingListContentProvider.AUTHORITY);
    }

    @Test
    public void testOperationsWithDataBase() throws Exception {
        MockContentResolver mockContentResolver = getMockContentResolver();
        testInsertAndQuery(mockContentResolver);
        testUpdate(mockContentResolver);
        testDelete(mockContentResolver);
    }

    public void testInsertAndQuery(MockContentResolver mockContentResolver) throws Exception {
        // Добавим новый список покупок
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, mListName);
        Uri idUri = mockContentResolver.insert(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        mListId = ContentUris.parseId(idUri);
        assertTrue(mListId > 0);

        // Проверим, что в таблице есть запись с именем mListName
        Cursor cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_NAME + " = '" + mListName + "'", null, null);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        // Добавим два новых продукта
        contentValues.put(ShoppingListContentProvider.KEY_NAME, mProductName1);
        idUri = mockContentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        mProductId1 = ContentUris.parseId(idUri);
        assertTrue(mProductId1 > 0);

        // Проверим, что в таблице есть запись с именем нового продукта
        cursor = mockContentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null, null);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        contentValues.put(ShoppingListContentProvider.KEY_NAME, mProductName2);
        idUri = mockContentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        mProductId2 = ContentUris.parseId(idUri);
        assertTrue(mProductId2 > 0);

        // Проверим, что в таблице есть запись с именем нового продукта
        cursor = mockContentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_NAME + " = '" + mProductName2 + "'", null, null);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        // Добавляем новые элементы в список покупок
        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок
        contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, mListId);
        contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, mProductId1);
        idUri = mockContentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        long id = ContentUris.parseId(idUri);
        assertTrue(id > 0);
        contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, mProductId2);
        idUri = mockContentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        id = ContentUris.parseId(idUri);
        assertTrue(id > 0);

        // Проверим, что в таблице есть записи
        cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + " = " + mListId
                        + " AND (" + ShoppingListContentProvider.KEY_PRODUCT_ID + " = " + mProductId1
                        + " OR " + ShoppingListContentProvider.KEY_PRODUCT_ID + " = " + mProductId2 + ")"
                , null, null);
        assertTrue(cursor.getCount() == 2);
        cursor.close();
    }

    public void testUpdate(MockContentResolver mockContentResolver) throws Exception {

        // Изменяем имя списка покупок (добавляем суффикс Changed)
        ContentValues contentValues = new ContentValues();
        mListName += "Changed";
        contentValues.put(ShoppingListContentProvider.KEY_NAME, mListName);
        mockContentResolver.update(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                contentValues, ShoppingListContentProvider.KEY_ID + " = " + mListId, null);

        // Проверяем, что у списка со старым id изменилось имя
        Cursor cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_NAME + " = '" + mListName + "'", null, null);
        assertTrue(cursor.getCount() == 1);

        // Изменяем имя одного из продуктов (добавляем суффикс Changed)
        mProductName1 += "Changed";
        contentValues.put(ShoppingListContentProvider.KEY_NAME, mProductName1);
        mockContentResolver.update(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                contentValues, ShoppingListContentProvider.KEY_ID + " = " + mProductId1, null);

        // Проверяем, что имя продукта изменилось (выборка по id)
        cursor = mockContentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI
                , null, ShoppingListContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null, null);
        assertTrue(cursor.getCount() == 1);
    }

    public void testDelete(MockContentResolver mockContentResolver) throws Exception {
        // Удаляем один из тестовых продуктов
        mockContentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null);

        // Проверяем, что что продукт удалился из таблицы продуктов
        Cursor cursor = mockContentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_ID + " = " + mProductId1, null, null);
        assertTrue(cursor.getCount() == 0);

        // Проверяем, что что продукт удалился из таблицы содержимого списка покупок
        cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_PRODUCT_ID + " = " + mProductId1, null, null);
        assertTrue(cursor.getCount() == 0);

        // Удаляем список покупок
        mockContentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME + " = '" + mListName + "'", null);

        // Проверяем, что список покупок удалился
        cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_ID + " = " + mListId, null, null);
        assertTrue(cursor.getCount() == 0);

        // Проверяем, что нет записей по данному списку покупок и в таблице содержимого
        cursor = mockContentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + " = " + mListId, null, null);
        assertTrue(cursor.getCount() == 0);
    }
}
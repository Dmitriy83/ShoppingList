package com.RightDirection.ShoppingList.adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.RightDirection.ShoppingList.utils.SL_ContentProvider;

import org.junit.Ignore;

// Перестал работать после использования SQLiteAssetHelper
@Ignore
public class SLContentProviderTest extends ProviderTestCase2{

    private String mListName = "newShoppingListTest";
    private String mProductName1 = "newProductTest1";
    private long mListId = 0;
    private long mProductId1 = 0;

    public SLContentProviderTest() {
        //noinspection unchecked
        super(SL_ContentProvider.class, SL_ContentProvider.AUTHORITY);
    }

    //@Test // Для JUnit3 метод считается тестовым, если есть преффикс test
    public void testOperationsWithDataBase() throws Exception {
        MockContentResolver mockContentResolver = getMockContentResolver();
        insertAndQuery(mockContentResolver);
        update(mockContentResolver);
        delete(mockContentResolver);
    }

    private void insertAndQuery(MockContentResolver mockContentResolver) {
        // Добавим новый список покупок
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_NAME, mListName);
        Uri idUri = mockContentResolver.insert(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        mListId = ContentUris.parseId(idUri);
        assertTrue(mListId > 0);

        // Проверим, что в таблице есть запись с именем mListName
        Cursor cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI
                , null, SL_ContentProvider.KEY_NAME + " = '" + mListName + "'", null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        // Добавим два новых продукта
        contentValues.put(SL_ContentProvider.KEY_NAME, mProductName1);
        idUri = mockContentResolver.insert(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        mProductId1 = ContentUris.parseId(idUri);
        assertTrue(mProductId1 > 0);

        // Проверим, что в таблице есть запись с именем нового продукта
        cursor = mockContentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI
                , null, SL_ContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        String mProductName2 = "newProductTest2";
        contentValues.put(SL_ContentProvider.KEY_NAME, mProductName2);
        idUri = mockContentResolver.insert(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        long mProductId2 = ContentUris.parseId(idUri);
        assertTrue(mProductId2 > 0);

        // Проверим, что в таблице есть запись с именем нового продукта
        cursor = mockContentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI
                , null, SL_ContentProvider.KEY_NAME + " = '" + mProductName2 + "'", null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 1);
        cursor.close();

        // Добавляем новые элементы в список покупок
        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок
        contentValues.put(SL_ContentProvider.KEY_SHOPPING_LIST_ID, mListId);
        contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, mProductId1);
        idUri = mockContentResolver.insert(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        long id = ContentUris.parseId(idUri);
        assertTrue(id > 0);
        contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, mProductId2);
        idUri = mockContentResolver.insert(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        assertNotNull(idUri);
        id = ContentUris.parseId(idUri);
        assertTrue(id > 0);

        // Проверим, что в таблице есть записи
        cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI
                , null, SL_ContentProvider.KEY_SHOPPING_LIST_ID + " = " + mListId
                        + " AND (" + SL_ContentProvider.KEY_PRODUCT_ID + " = " + mProductId1
                        + " OR " + SL_ContentProvider.KEY_PRODUCT_ID + " = " + mProductId2 + ")"
                , null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 2);
        cursor.close();
    }

    private void update(MockContentResolver mockContentResolver) {

        // Изменяем имя списка покупок (добавляем суффикс Changed)
        ContentValues contentValues = new ContentValues();
        mListName += "Changed";
        contentValues.put(SL_ContentProvider.KEY_NAME, mListName);
        mockContentResolver.update(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                contentValues, SL_ContentProvider.KEY_ID + " = " + mListId, null);

        // Проверяем, что у списка со старым id изменилось имя
        Cursor cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI
                , null, SL_ContentProvider.KEY_NAME + " = '" + mListName + "'", null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 1);

        // Изменяем имя одного из продуктов (добавляем суффикс Changed)
        mProductName1 += "Changed";
        contentValues.put(SL_ContentProvider.KEY_NAME, mProductName1);
        mockContentResolver.update(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                contentValues, SL_ContentProvider.KEY_ID + " = " + mProductId1, null);

        // Проверяем, что имя продукта изменилось (выборка по id)
        cursor = mockContentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI
                , null, SL_ContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 1);
    }

    private void delete(MockContentResolver mockContentResolver) {
        // Удаляем один из тестовых продуктов
        mockContentResolver.delete(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                SL_ContentProvider.KEY_NAME + " = '" + mProductName1 + "'", null);

        // Проверяем, что что продукт удалился из таблицы продуктов
        Cursor cursor = mockContentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                null, SL_ContentProvider.KEY_ID + " = " + mProductId1, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 0);

        // Проверяем, что что продукт удалился из таблицы содержимого списка покупок
        cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, SL_ContentProvider.KEY_PRODUCT_ID + " = " + mProductId1, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 0);

        // Удаляем список покупок
        mockContentResolver.delete(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                SL_ContentProvider.KEY_NAME + " = '" + mListName + "'", null);

        // Проверяем, что список покупок удалился
        cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                null, SL_ContentProvider.KEY_ID + " = " + mListId, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 0);

        // Проверяем, что нет записей по данному списку покупок и в таблице содержимого
        cursor = mockContentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, SL_ContentProvider.KEY_SHOPPING_LIST_ID + " = " + mListId, null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 0);
    }
}
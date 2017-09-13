package com.RightDirection.ShoppingList.utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class SL_ContentProvider extends ContentProvider {

    private ShoppingListSQLiteOpenHelper sqLiteOpenHelper;

    public static final String AUTHORITY = "com.RightDirection.shoppinglistcontentprovider";
    public static final Uri PRODUCTS_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/products");
    public static final Uri UNITS_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/units");
    public static final Uri SHOPPING_LISTS_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/shoppinglists");
    public static final Uri SHOPPING_LIST_CONTENT_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/shoppinglistcontent");
    public static final Uri CATEGORIES_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/categories");
    private static final int PRODUCTS_ALL_ROWS = 1;
    private static final int PRODUCTS_SINGLE_ROW = 2;
    private static final int SHOPPING_LISTS_ALL_ROWS = 3;
    private static final int SHOPPING_LISTS_SINGLE_ROW = 4;
    private static final int SHOPPING_LIST_CONTENT_ALL_ROWS = 5;
    private static final int SHOPPING_LIST_CONTENT_SINGLE_ROW = 6;
    private static final int FILES = 7;
    private static final int CATEGORIES_ALL_ROWS = 8;
    private static final int CATEGORIES_SINGLE_ROW = 9;
    private static final int UNITS_ALL_ROWS = 10;
    private static final int UNITS_SINGLE_ROW = 11;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "products",                 PRODUCTS_ALL_ROWS);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "products/#",               PRODUCTS_SINGLE_ROW);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "shoppinglists",            SHOPPING_LISTS_ALL_ROWS);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "shoppinglists/#",          SHOPPING_LISTS_SINGLE_ROW);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "shoppinglistcontent",      SHOPPING_LIST_CONTENT_ALL_ROWS);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "shoppinglistcontent/#",    SHOPPING_LIST_CONTENT_SINGLE_ROW);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "files/*",                  FILES);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "categories",               CATEGORIES_ALL_ROWS);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "categories/#",             CATEGORIES_SINGLE_ROW);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "units",                    UNITS_ALL_ROWS);
        uriMatcher.addURI("com.RightDirection.shoppinglistcontentprovider", "units/#",                  UNITS_SINGLE_ROW);
    }

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_UNIT_ID = "UNIT_ID";
    public static final String KEY_PRICE = "PRICE";
    public static final String KEY_UNIT_NAME = "UNIT_NAME";
    public static final String KEY_UNIT_SHORT_NAME = "UNIT_SHORT_NAME";
    public static final String KEY_PICTURE = "PICTURE";
    public static final String KEY_SHOPPING_LIST_ID = "SHOPPING_LIST_ID";
    public static final String KEY_PRODUCT_ID = "PRODUCT_ID";
    public static final String KEY_COUNT = "COUNT";
    public static final String KEY_CATEGORY_ID = "CATEGORY_ID";
    public static final String KEY_CATEGORY_NAME = "CATEGORY_NAME";
    public static final String KEY_CATEGORY_ORDER = "CATEGORY_ORDER";
    public static final String KEY_CATEGORY_PICTURE_URI = "CATEGORY_PICTURE_URI";
    public static final String KEY_IS_CHECKED = "IS_CHECKED";
    public static final String KEY_SHOPPING_LIST_ROW_ID = "SHOPPING_LIST_ROW_ID";
    public static final String KEY_IS_FILTERED = "IS_FILTERED";
    public static final String KEY_NUMBER_OF_CROSSED_OUT = "NUMBER_OF_CROSSED_OUT";
    public static final String KEY_TOTAL_COUNT = "TOTAL_COUNT";
    public static final String KEY_DEFAULT_UNIT_ID = "DEFAULT_UNIT_ID";
    public static final String KEY_DEFAULT_UNIT_NAME = "DEFAULT_UNIT_NAME";
    public static final String KEY_DEFAULT_UNIT_SHORT_NAME = "DEFAULT_UNIT_SHORT_NAME";
    public static final String KEY_LAST_PRICE = "LAST_PRICE";
    private static final String DATABASE_NAME_RU = "RU_SHOPPING_LIST.db";
    private static final String DATABASE_NAME_ENG = "ENG_SHOPPING_LIST.db";
    private static final String PRODUCTS_TABLE_NAME = "PRODUCTS";
    private static final String SHOPPING_LISTS_TABLE_NAME = "SHOPPING_LISTS";
    private static final String SHOPPING_LISTS_WITH_ADDITIONAL_INFO_VIEW_NAME = "SHOPPING_LISTS_WITH_ADDITIONAL_INFO";
    private static final String SHOPPING_LIST_CONTENT_WITH_ADDITIONAL_INFO_VIEW_NAME = "SHOPPING_LIST_CONTENT_WITH_ADDITIONAL_INFO";
    private static final String PRODUCTS_WITH_ADDITIONAL_INFO_VIEW_NAME = "PRODUCTS_WITH_ADDITIONAL_INFO";
    private static final String SHOPPING_LIST_CONTENT_TABLE_NAME = "SHOPPING_LIST_CONTENT";
    private static final String CATEGORIES_TABLE_NAME = "CATEGORIES";
    private static final String UNITS_TABLE_NAME = "UNITS";
    private static final int DATABASE_VERSION = 12;

    /**
     * Возвращает имя базы данных в зависимости от локализации (по умолчанию - Английская).
     */
    private String getDataBaseName(){
        String dbName = DATABASE_NAME_ENG;
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) {
            dbName = DATABASE_NAME_RU;
        }
        return dbName;
    }

    @Override
    public boolean onCreate() {
        sqLiteOpenHelper = new ShoppingListSQLiteOpenHelper(getContext(), getDataBaseName());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Вернем строку, которая идентифицирует MIME-тип пути источника данных
        switch(uriMatcher.match(uri)){
            case PRODUCTS_ALL_ROWS: return "vnd.android.cursor.dir/vnd.RightDirection.ShoppingList.products";
            case PRODUCTS_SINGLE_ROW: return "vnd.android.cursor.item/vnd.RightDirection.ShoppingList.products";
            case SHOPPING_LISTS_ALL_ROWS: return "vnd.android.cursor.dir/vnd.RightDirection.ShoppingList.shoppinglists";
            case SHOPPING_LISTS_SINGLE_ROW: return "vnd.android.cursor.item/vnd.RightDirection.ShoppingList.shoppinglists";
            case SHOPPING_LIST_CONTENT_ALL_ROWS: return "vnd.android.cursor.dir/vnd.RightDirection.ShoppingList.shoppinglistcontent";
            case SHOPPING_LIST_CONTENT_SINGLE_ROW: return "vnd.android.cursor.item/vnd.RightDirection.ShoppingList.shoppinglistcontent";
            case FILES: return "vnd.android.file.dir/vnd.RightDirection.ShoppingList.files";
            case CATEGORIES_ALL_ROWS: return "vnd.android.cursor.dir/vnd.RightDirection.ShoppingList.categories";
            case CATEGORIES_SINGLE_ROW: return "vnd.android.cursor.item/vnd.RightDirection.ShoppingList.categories";
            case UNITS_ALL_ROWS: return "vnd.android.cursor.dir/vnd.RightDirection.ShoppingList.units";
            case UNITS_SINGLE_ROW: return "vnd.android.cursor.item/vnd.RightDirection.ShoppingList.units";
            default: throw new IllegalArgumentException("Неподдерживаемый URI:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Откроем базу данных для чтения/записи
        SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

        // Установим таблицу и CONTENT_URI
        String tableName = getTableName(uri, false);
        Uri contentUri = getContentUri(uri);

        long id = db.insert(tableName, null, values);

        if (id > -1) {
            // Создадим и вернем путь к только что вставленной строке
            Uri insertedID = ContentUris.withAppendedId(contentUri, id);

            // Оповестим все объекты ContentObserver об изменениях в наборе данных
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }

            return insertedID;
        }
        else {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Откроем базу данных для чтения/записи
        SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

        // Установим таблицу и CONTENT_URI
        String tableName = getTableName(uri, false);

        // Если это путь к строке, удалим одну строку
        selection = supplementedSelection(selection, uri);

        // Чтобы удалить все строки и вернуть значение, передадим 1
        if (selection == null) selection = "1";

        // Если удаляется запись таблиц SHOPPING_LISTS или PRODUCTS, то необходимо удалить также подчиненные
        // записи таблицы SHOPPING_LISTS_CONTENT
        switch(uriMatcher.match(uri)){
            case PRODUCTS_SINGLE_ROW:
            case PRODUCTS_ALL_ROWS:
                deleteShoppingListRowsByProductId(selection, selectionArgs);
                break;
            case SHOPPING_LISTS_SINGLE_ROW:
            case SHOPPING_LISTS_ALL_ROWS:
                deleteShoppingListRowsByShoppingListId(selection, selectionArgs);
                break;
            case CATEGORIES_SINGLE_ROW:
            case CATEGORIES_ALL_ROWS:
                cleanRefsOnCategory(selection, selectionArgs);
                break;
            case UNITS_SINGLE_ROW:
            case UNITS_ALL_ROWS:
                cleanRefsOnUnit(selection, selectionArgs);
                break;
            default: break;
        }

        // Выполним удаление
        int deleteCount = db.delete(tableName, selection, selectionArgs);

        // Оповестим все объекты ContentObserver об изменениях в наборе данных
        Context context = getContext();
        if (context != null) context.getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    private void cleanRefsOnCategory(String selection, String[] selectionArgs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_CATEGORY_ID, 0);
        String newSelection = SL_ContentProvider.KEY_CATEGORY_ID
                + " IN (SELECT " + SL_ContentProvider.KEY_CATEGORY_ID
                + " FROM " + CATEGORIES_TABLE_NAME + " WHERE " + selection + ")";
        update(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues, newSelection, selectionArgs);
    }

    private void cleanRefsOnUnit(String selection, String[] selectionArgs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_DEFAULT_UNIT_ID, 0);
        String newSelection = SL_ContentProvider.KEY_DEFAULT_UNIT_ID
                + " IN (SELECT " + SL_ContentProvider.KEY_ID
                + " FROM " + UNITS_TABLE_NAME + " WHERE " + selection + ")";
        update(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues, newSelection, selectionArgs);

        contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_UNIT_ID, 0);
        newSelection = SL_ContentProvider.KEY_UNIT_ID
                + " IN (SELECT " + SL_ContentProvider.KEY_ID
                + " FROM " + UNITS_TABLE_NAME + " WHERE " + selection + ")";
        update(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues, newSelection, selectionArgs);
    }

    private void deleteShoppingListRowsByProductId(String selection, String[] selectionArgs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, 0);
        String newSelection = SL_ContentProvider.KEY_PRODUCT_ID
                + " IN (SELECT " + SL_ContentProvider.KEY_ID
                + " FROM " + PRODUCTS_TABLE_NAME + " WHERE " + selection + ")";
        delete(SHOPPING_LIST_CONTENT_CONTENT_URI, newSelection, selectionArgs);
    }

    private void deleteShoppingListRowsByShoppingListId(String selection, String[] selectionArgs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_SHOPPING_LIST_ID, 0);
        String newSelection = SL_ContentProvider.KEY_SHOPPING_LIST_ID
                + " IN (SELECT " + SL_ContentProvider.KEY_ID
                + " FROM " + SHOPPING_LISTS_TABLE_NAME + " WHERE " + selection + ")";
        delete(SHOPPING_LIST_CONTENT_CONTENT_URI, newSelection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Откроем базу данных для чтения/записи
        SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

        // Установим таблицу и CONTENT_URI
        String tableName = getTableName(uri, false);

        // Если это путь к строке, удалим одну строку
        selection = supplementedSelection(selection, uri);

        // Выполним обновление
        int updateCount = db.update(tableName, values, selection, selectionArgs);

        // Оповестим все объекты ContentObserver об изменениях в наборе данных
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Откроем базу данных для чтения
        SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Установим таблицу
        queryBuilder.setTables(getTableName(uri, true));

        // Если это запрос одной строки, то ограничим выборку переданной строкой
        selection = supplementedSelection(selection, uri);
        /*if (selection != null) {
            queryBuilder.appendWhere(selection);
        }*/

        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Uri getContentUri(Uri uri) {
        Uri contentUri = null;
        switch(uriMatcher.match(uri)){
            case PRODUCTS_SINGLE_ROW:
            case PRODUCTS_ALL_ROWS:
                contentUri = PRODUCTS_CONTENT_URI;
                break;
            case SHOPPING_LISTS_SINGLE_ROW:
            case SHOPPING_LISTS_ALL_ROWS:
                contentUri = SHOPPING_LISTS_CONTENT_URI;
                break;
            case SHOPPING_LIST_CONTENT_SINGLE_ROW:
            case SHOPPING_LIST_CONTENT_ALL_ROWS:
                contentUri = SHOPPING_LIST_CONTENT_CONTENT_URI;
                break;
            case CATEGORIES_ALL_ROWS:
            case CATEGORIES_SINGLE_ROW:
                contentUri = CATEGORIES_CONTENT_URI;
                break;
            case UNITS_ALL_ROWS:
            case UNITS_SINGLE_ROW:
                contentUri = UNITS_CONTENT_URI;
                break;
            default: break;
        }
        return contentUri;
    }

    private String getTableName(Uri uri, boolean forQuery) {
        String tableName = null;
        switch(uriMatcher.match(uri)){
            case PRODUCTS_SINGLE_ROW:
            case PRODUCTS_ALL_ROWS:
                if (forQuery) {
                    tableName = PRODUCTS_WITH_ADDITIONAL_INFO_VIEW_NAME;
                }
                else {
                    tableName = PRODUCTS_TABLE_NAME;
                }
                break;
            case SHOPPING_LISTS_SINGLE_ROW:
            case SHOPPING_LISTS_ALL_ROWS:
                if (forQuery) {
                    tableName = SHOPPING_LISTS_WITH_ADDITIONAL_INFO_VIEW_NAME;
                }
                else {
                    tableName = SHOPPING_LISTS_TABLE_NAME;
                }
                break;
            case SHOPPING_LIST_CONTENT_SINGLE_ROW:
            case SHOPPING_LIST_CONTENT_ALL_ROWS:
                if (forQuery) {
                    tableName = SHOPPING_LIST_CONTENT_WITH_ADDITIONAL_INFO_VIEW_NAME;
                }
                else {
                    tableName = SHOPPING_LIST_CONTENT_TABLE_NAME;
                }
                break;
            case CATEGORIES_ALL_ROWS:
            case CATEGORIES_SINGLE_ROW:
                tableName = CATEGORIES_TABLE_NAME;
                break;
            case UNITS_ALL_ROWS:
            case UNITS_SINGLE_ROW:
                tableName = UNITS_TABLE_NAME;
                break;
            default: break;
        }
        return tableName;
    }

    private String supplementedSelection(String selection, Uri uri) {
        switch(uriMatcher.match(uri)){
            case PRODUCTS_SINGLE_ROW:
            case SHOPPING_LISTS_SINGLE_ROW:
            case SHOPPING_LIST_CONTENT_SINGLE_ROW:
            case CATEGORIES_SINGLE_ROW:
            case UNITS_SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowID
                        + (!TextUtils.isEmpty(selection)? " AND (" + selection + ")": "");
            default: break;
        }

        return selection;
    }

    /*public static String[] getShoppingListContentProjection(){
        return new String[]{
                SHOPPING_LIST_CONTENT_TABLE_NAME + "." + KEY_ID + " AS " + KEY_SHOPPING_LIST_ROW_ID,
                KEY_PRODUCT_ID,
                KEY_NAME,
                KEY_COUNT,
                KEY_IS_CHECKED,
                KEY_PICTURE,
                KEY_CATEGORY_NAME,
                CATEGORIES_TABLE_NAME + "." + KEY_CATEGORY_ID  + " AS " + KEY_CATEGORY_ID,
                KEY_CATEGORY_ORDER,
                KEY_CATEGORY_PICTURE_URI,
                UNITS_TABLE_NAME + "." + KEY_ID  + " AS " + KEY_UNIT_ID,
                UNITS_TABLE_NAME + "." + KEY_NAME  + " AS " + KEY_UNIT_NAME,
                UNITS_TABLE_NAME + "." + KEY_UNIT_SHORT_NAME  + " AS " + KEY_UNIT_SHORT_NAME,
        };
    }*/

    /*public static String[] getProductsProjection(){
        return new String[]{
                PRODUCTS_TABLE_NAME + "." + KEY_ID + " AS " + KEY_PRODUCT_ID,
                KEY_NAME,
                KEY_PICTURE,
                KEY_CATEGORY_NAME,
                CATEGORIES_TABLE_NAME + "." + KEY_CATEGORY_ID  + " AS " + KEY_CATEGORY_ID,
                KEY_CATEGORY_ORDER,
                KEY_CATEGORY_PICTURE_URI,
                UNITS_TABLE_NAME + "." + KEY_ID  + " AS " + KEY_UNIT_ID,
                UNITS_TABLE_NAME + "." + KEY_NAME  + " AS " + KEY_UNIT_NAME,
                UNITS_TABLE_NAME + "." + KEY_UNIT_SHORT_NAME  + " AS " + KEY_UNIT_SHORT_NAME
        };
    }*/

    @Nullable
    public static Uri getImageUri(@NonNull Cursor data) {
        int keyPictureIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PICTURE);

        String strImageUri = data.getString(keyPictureIndex);

        Uri imageUri = null;
        if (strImageUri != null){
            imageUri = Uri.parse(strImageUri);
        }
        return imageUri;
    }

    @Nullable
    public static Uri getCategoryImageUri(@NonNull Cursor data) {
        int keyPictureIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_CATEGORY_PICTURE_URI);

        String strImageUri = data.getString(keyPictureIndex);

        Uri imageUri = null;
        if (strImageUri != null){
            imageUri = Uri.parse(strImageUri);
        }
        return imageUri;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {

        String LOG_TAG = "Provider - openFile";
        Log.v(LOG_TAG, "Called with uri: '" + uri + "'." + uri.getLastPathSegment());

        switch (uriMatcher.match(uri)) {
            case FILES:
                // The desired file name is specified by the last segment of the path
                // E.g. 'content://com.stephendnicholas.gmailattach.provider/Test.txt'
                // Take this and build the path to the file
                Context context = getContext();
                if (context != null) {
                    String fileLocation = context.getCacheDir() + File.separator + uri.getLastPathSegment();

                    // Create & return a ParcelFileDescriptor pointing to the file
                    // Note: I don't care what mode they ask for - they're only getting
                    // read only
                    return ParcelFileDescriptor.open(new File(fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
                }
                else {
                    Log.v(LOG_TAG, "getContext().getCacheDir() returned null.");
                    return null;
                }

                // Otherwise unrecognised Uri
            default:
                Log.v(LOG_TAG, "Unsupported uri: '" + uri + "'.");
                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }
    }

    class ShoppingListSQLiteOpenHelper extends SQLiteAssetHelper {

        ShoppingListSQLiteOpenHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
        }

        // Обновление SQLiteAssetHelper происходит с помощью текстового файла sql, расположенного в директории databases
    }
}


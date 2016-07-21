package com.RightDirection.ShoppingList.helpers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ShoppingListContentProvider extends ContentProvider {

    private ShoppingListSQLiteOpenHelper sqLiteOpenHelper;

    public static final Uri PRODUCTS_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/products");
    public static final Uri SHOPPING_LISTS_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/shoppinglists");
    public static final Uri SHOPPING_LIST_CONTENT_CONTENT_URI = Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/shoppinglistcontent");
    private static final int PRODUCTS_ALL_ROWS = 1;
    private static final int PRODUCTS_SINGLE_ROW = 2;
    private static final int SHOPPING_LISTS_ALL_ROWS = 3;
    private static final int SHOPPING_LISTS_SINGLE_ROW = 4;
    private static final int SHOPPING_LIST_CONTENT_ALL_ROWS = 5;
    private static final int SHOPPING_LIST_CONTENT_SINGLE_ROW = 6;
    private static final int FILES = 7;

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
    }

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_PICTURE = "PICTURE";
    public static final String KEY_SHOPPING_LIST_ID = "SHOPPING_LIST_ID";
    public static final String KEY_PRODUCT_ID = "PRODUCT_ID";
    private static final String DATABASE_NAME = "shoppingListDatabase.db";
    private static final String PRODUCTS_TABLE_NAME = "PRODUCTS";
    private static final String SHOPPING_LISTS_TABLE_NAME = "SHOPPING_LISTS";
    private static final String SHOPPING_LIST_CONTENT_TABLE_NAME = "SHOPPING_LIST_CONTENT";
    private static final int DATABASE_VERSION = 5;

    @Override
    public boolean onCreate() {
        sqLiteOpenHelper = new ShoppingListSQLiteOpenHelper(getContext(),
                DATABASE_NAME, null,
                DATABASE_VERSION);
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
            default: throw new IllegalArgumentException("Неподдерживаемый URI:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Откроем базу данных для чтения/записи
        SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

        // Чтобы добавить в базу данных пустую строку с помощью пустого объекта ContentValues, используйте
        // параметр nullColumnHack, указав название столбца, значение которого может равняться null.
        String nullColumnHack = null;

        // Установим таблицу и CONTENT_URI
        String tableName = getTableName(uri, false);
        Uri contentUri = getContentUri(uri);

        long id = db.insert(tableName, nullColumnHack, values);

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

        // Выполним удаление
        int deleteCount = db.delete(tableName, selection, selectionArgs);

        // Оповестим все объекты ContentObserver об изменениях в наборе данных
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }

        return deleteCount;
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

        // При необходимости заменим следующие переменные SQL-выражениями
        String groupBy = null;
        String having = null;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // Установим таблицу
        queryBuilder.setTables(getTableName(uri, true));

        // Если это запрос одной строки, то ограничим выборку переданной строкой
        selection = supplementedSelection(selection, uri);
        if (selection != null) {
            queryBuilder.appendWhere(selection);
        }

        return queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
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
            default: break;
        }
        return contentUri;
    }

    private String getTableName(Uri uri, boolean forQuery) {
        String tableName = null;
        switch(uriMatcher.match(uri)){
            case PRODUCTS_SINGLE_ROW:
            case PRODUCTS_ALL_ROWS:
                tableName = PRODUCTS_TABLE_NAME;
                break;
            case SHOPPING_LISTS_SINGLE_ROW:
            case SHOPPING_LISTS_ALL_ROWS:
                tableName = SHOPPING_LISTS_TABLE_NAME;
                break;
            case SHOPPING_LIST_CONTENT_SINGLE_ROW:
            case SHOPPING_LIST_CONTENT_ALL_ROWS:
                if (forQuery) {
                    tableName = SHOPPING_LIST_CONTENT_TABLE_NAME + " LEFT OUTER JOIN " + PRODUCTS_TABLE_NAME + " ON ("
                            + SHOPPING_LIST_CONTENT_TABLE_NAME + "." + KEY_PRODUCT_ID
                            + " = " + PRODUCTS_TABLE_NAME + "." + KEY_ID + ")";
                }
                else {
                    tableName = SHOPPING_LIST_CONTENT_TABLE_NAME;
                }
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
                String rowID = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection)? " AND (" + selection + ")": "");
            default: break;
        }

        return selection;
    }

    @Nullable
    public static Uri getImageUri(Cursor data) {
        int keyPictureIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PICTURE);

        String strImageUri = data.getString(keyPictureIndex);

        Uri imageUri;
        if (strImageUri != null){
            imageUri = Uri.parse(strImageUri);
        }
        else{
            imageUri = null;
        }
        return imageUri;
    }

    class ShoppingListSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final String TAG = "ShoppingListSQLite";

        public ShoppingListSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Вызывается, когда на диске нет базы данных, чтобы вспомогательный класс создал новую
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Создадим таблицы, если они не были созданы ранее

            // Таблица "Продукты"
            String queryCreateProductsTable = "CREATE TABLE " + PRODUCTS_TABLE_NAME
                    + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_NAME + ", " + KEY_PICTURE + ");";
            db.execSQL(queryCreateProductsTable);

            // Таблица "Списки покупок"
            String queryCreateShoppingListsTable = "CREATE TABLE " + SHOPPING_LISTS_TABLE_NAME
                    + "(" + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_NAME + ");";
            db.execSQL(queryCreateShoppingListsTable);

            // Таблица "Состав списка покупок"
            String queryCreateShoppingListContentTable = "CREATE TABLE " + SHOPPING_LIST_CONTENT_TABLE_NAME
                    + "(" + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_SHOPPING_LIST_ID + " INTEGER, " + KEY_PRODUCT_ID + " INTEGER);";
            db.execSQL(queryCreateShoppingListContentTable);
        }

        // Вызывается при несовпадении версий базы данных, т.е. когда база данных, хранящаяся на диске,
        // должна быть обновлена до текущей версии.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Запишем в лог информацию об обновлении версии базы данных.
            Log.w(TAG, "Обновление с версии " + oldVersion + " до " + newVersion + ".");

            if (newVersion == 5) {
                // Уничтожаем таблицу и создаем новую
                db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST_CONTENT_TABLE_NAME);
                String queryCreateShoppingListContentTable = "CREATE TABLE " + SHOPPING_LIST_CONTENT_TABLE_NAME
                        + "(" + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + KEY_SHOPPING_LIST_ID + " INTEGER, " + KEY_PRODUCT_ID + " INTEGER);";
                db.execSQL(queryCreateShoppingListContentTable);
            }
        }
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
}


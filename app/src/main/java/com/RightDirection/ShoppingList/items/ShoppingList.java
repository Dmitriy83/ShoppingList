package com.RightDirection.ShoppingList.items;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.ShoppingListContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ShoppingList extends ListItem implements IDataBaseOperations {

    private ArrayList<Product> mProducts;

    public ShoppingList(long id, String name) {
        super(id, name);
    }

    public ShoppingList(long id, String name, ArrayList<Product> products) {
        super(id, name);
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

    @Override
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
        HashMap<String, Long> map = new HashMap<>(data.getCount());
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        while (data.moveToNext()){
            map.put(data.getString(keyNameIndex), data.getLong(keyIdIndex));
        }
        data.close();

        for (Product product: mProducts) {
            long id = map.get(product.getName());
            product.setId(id);
        }
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

    public void sendByEmail(Context context){

        try{
            // Пока отключим возможность получения и загрузки писем с файлом json,
            // т.к. настройка эл. почты слишком сложна для простого пользователя.
            /*
            // Создадим JSON файл по списку покупок
            String fileName = context.getString(R.string.json_file_identifier) + " '" + getName()
                    + "'" + ".json";
            createShoppingListJSONFile(context, fileName);
            */
            String fileName = null;

            context.startActivity(getSendEmailIntent("d.zhiharev@mail.ru",
                    context.getString(R.string.json_file_identifier) + " '" + getName() + "'",
                    convertShoppingListToString(context), fileName));
        }
        catch(Exception e){
            System.out.println("Exception raises during sending mail. Discription: " + e);
        }
    }

    private String convertShoppingListToString(Context context){
        if (mProducts == null || mProducts.size() == 0) {
            // Попробуем получить товары из БД. Такое возможно, когда вызов метода происходит
            // основной активности (товары для списков в ней не загружаются)
            getProductsFromDB(context);
        }

        String result = "";
        String divider = context.getString(R.string.divider);
        String productDivider = context.getString(R.string.product_divider);
        boolean firstLine = true;
        for (Product product: mProducts) {
            if (!firstLine) result = result + "\n";
            else firstLine = false;

            result = result + product.getName() + divider + " " + String.valueOf(product.getCount())
                    + productDivider;
        }

        return result;
    }

    private void getProductsFromDB(Context context) {
        if (mProducts != null) mProducts.clear();
        else mProducts = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);

        // Читаем данные из базы и записываем в объект JSON
        while (data.moveToNext()){
            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex), null, data.getFloat(keyCountIndex));
            mProducts.add(newProduct);
        }

        // Закроем курсор
        data.close();
    }

    private void createShoppingListJSONFile(Context context, String fileName) throws JSONException {

        JSONArray listItemsArray;
        if (mProducts == null || mProducts.size() == 0){
            // Попробуем получить товары из БД. Такое возможно, когда вызов метода происходит
            // основной активности (товары для списков в ней не загружаются)
            listItemsArray = getJSONArrayFromDB(context);
        }else{
            // Сформируем JSONArray из mProducts
            listItemsArray = getJSONArrayFromField();
        }

        JSONObject jsonList = new JSONObject();
        jsonList.put(ShoppingListContentProvider.KEY_ID,   getId());
        jsonList.put(ShoppingListContentProvider.KEY_NAME, getName());
        jsonList.put("items", listItemsArray);

        String jsonStr = jsonList.toString();
        Log.i("CREATING_JSON", jsonStr);

        // Запишем текст в файл
        try {
            createCachedFile(context, fileName, jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getJSONArrayFromField() throws JSONException{
        JSONArray array = new JSONArray();

        for (Product product: mProducts) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put(ShoppingListContentProvider.KEY_PRODUCT_ID,  product.getId());
            jsonItem.put(ShoppingListContentProvider.KEY_NAME,        product.getName());
            jsonItem.put(ShoppingListContentProvider.KEY_COUNT,       product.getCount());

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }
        return array;
    }

    private JSONArray getJSONArrayFromDB(Context context) throws JSONException{
        JSONArray array = new JSONArray();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);

        // Читаем данные из базы и записываем в объект JSON
        while (data.moveToNext()){
            JSONObject jsonItem = new JSONObject();

            jsonItem.put(ShoppingListContentProvider.KEY_PRODUCT_ID, data.getString(keyIdIndex));
            jsonItem.put(ShoppingListContentProvider.KEY_NAME, data.getString(keyNameIndex));
            jsonItem.put(ShoppingListContentProvider.KEY_COUNT, data.getString(keyCountIndex));

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }

        // Закроем курсор
        data.close();

        return array;
    }

    private Intent getSendEmailIntent(@Nullable String email, String subject, String body, String fileName) {

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        //Explicitly only use Gmail to send
        //emailIntent.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");

        emailIntent.setType("plain/text");

        //Add the recipients
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        if (fileName != null) {
            //Add the attachment by specifying a reference to our custom ContentProvider
            //and the specific file of interest
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/files/" + fileName));
        }

        return emailIntent;
    }

    private void createCachedFile(Context context, String fileName, String content) throws IOException {

        File cacheFile = new File(context.getCacheDir() + File.separator + fileName);
        cacheFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(cacheFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);

        pw.flush();
        pw.close();
    }
}

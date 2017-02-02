package com.RightDirection.ShoppingList.items;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.LoadShoppingListActivity;
import com.RightDirection.ShoppingList.activities.OpeningOptionChoiceActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;

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

    private ArrayList<IListItem> mProducts;

    public ShoppingList(long id, String name) {
        super(id, name);
    }

    public ShoppingList(long id, String name, ArrayList<IListItem> products) {
        super(id, name);
        mProducts = products;
    }

    public ShoppingList(Cursor data){
        super(data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_ID)),
                data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME)));
    }

    private ShoppingList(Parcel in) {
        super(in);
    }

    public static final Creator<ShoppingList> CREATOR = new Creator<ShoppingList>() {
        @Override
        public ShoppingList createFromParcel(Parcel in) {
            return new ShoppingList(in);
        }

        @Override
        public ShoppingList[] newArray(int size) {
            return new ShoppingList[size];
        }
    };

    private void addProduct(Product product){
        if (mProducts == null) mProducts = new ArrayList<>();

        mProducts.add(product);
    }

    public ArrayList<IListItem> getProducts(){
        return mProducts;
    }

    public void setProducts(ArrayList<IListItem> products){
        mProducts = products;
        /*
        mProducts = new ArrayList<>();
        for (IListItem item: products) {
            if (item instanceof Product) mProducts.add((Product)item);
        }
        */
    }

    @Override
    public void addToDB(Context context) {
        // Сохраним список продуктов в БД
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Заполним значения для сохранения в базе данных
        // и запишем новый список покупок в таблицу SHOPPING_LISTS
        contentValues.put(SL_ContentProvider.KEY_NAME, getName());
        Uri insertedId = contentResolver.insert(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));

        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

        if (mProducts == null) return;

        // Запишем составлящие списка покупок в базу данных
        for (IListItem item : mProducts) {
            contentValues.put(SL_ContentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(SL_ContentProvider.KEY_COUNT, item.getCount());
            contentValues.put(SL_ContentProvider.KEY_IS_CHECKED, item.isChecked());
            contentResolver.insert(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }

        // Список покупок более не новый
        isNew = false;
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                SL_ContentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        if (mProducts == null) return;

        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Сначала удалим все записи редактируемого списка покупок из БД
        contentResolver.delete(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null);

        // Запишем составлящие списка покупок в базу данных
        for (IListItem item: mProducts) {
            contentValues.put(SL_ContentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(SL_ContentProvider.KEY_COUNT, item.getCount());
            contentValues.put(SL_ContentProvider.KEY_IS_CHECKED, item.isChecked());
            contentResolver.insert(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }
    }

    public void updateCheckedInDB(Context context) {
        if (mProducts == null) return;

        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        for (IListItem item: mProducts) {
            if (item instanceof Product) {
                Product product = (Product)item;
                contentValues.put(SL_ContentProvider.KEY_IS_CHECKED, item.isChecked());
                contentResolver.update(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues,
                        SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId() + " AND "
                                + SL_ContentProvider.KEY_ID + "=" + product.getRowId(),
                        null);
            }
        }
    }

    public void removeCheckedFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        for (IListItem item: mProducts) {
            if (item instanceof Product && item.isChecked()) {
                Product product = (Product)item;
                contentResolver.delete(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                        SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId() + " AND "
                                + SL_ContentProvider.KEY_ID + "=" + product.getRowId(),
                        null);
            }
        }

        // Удалим продукты из массива
        for (int i = mProducts.size()-1; i >= 0; i--) {
            Product product = (Product) mProducts.get(i);
            if (product.isChecked) mProducts.remove(i);
        }
    }

    @Override
    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(SL_ContentProvider.KEY_NAME, getName());
        contentResolver.update(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, SL_ContentProvider.KEY_ID +  " = " + getId(), null);
    }

    public void addNotExistingProductsToDB(Context context) {
        if (mProducts == null || mProducts.size() == 0) return;

        // Создадим строку условия
        String where = getWhereConditionForName();

        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        ArrayList<String> foundProducts = new ArrayList<>();
        assert data != null;
        int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
        while (data.moveToNext()){
            foundProducts.add(data.getString(keyNameIndex));
        }
        data.close();

        // Добавим несуществующие продукты в базу данных
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < mProducts.size(); i++) {
            String name = mProducts.get(i).getName();
            if (!foundProducts.contains(name)){
                contentValues.put(SL_ContentProvider.KEY_NAME, name);
                contentResolver.insert(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues);
            }
        }

        setProductsIdFromDB(context);
    }

    private void setProductsIdFromDB(Context context) {
        if (mProducts == null || mProducts.size() == 0) return;

        // Создадим строку условия
        String where = getWhereConditionForName();
        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        assert data != null;
        HashMap<String, Long> map = new HashMap<>(data.getCount());
        int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
        while (data.moveToNext()){
            map.put(data.getString(keyNameIndex), data.getLong(keyIdIndex));
        }
        data.close();

        for (IListItem product: mProducts) {
            long id = map.get(product.getName());
            product.setId(id);
        }
    }

    private String getWhereConditionForName() {
        if (mProducts == null) return "";

        String where = null;
        if (mProducts.size() > 0) {
            where = SL_ContentProvider.KEY_NAME + " IN (";
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

            String fileName = null;
            */

            String subject  = context.getString(R.string.json_file_identifier)
                    + " '" + getName() + "'";
            String body     = convertShoppingListToString(context);

            /*
            context.startActivity(Utils.getSendEmailIntent("",
                    context.getString(R.string.json_file_identifier) + " '" + getName() + "'",
                    convertShoppingListToString(context), fileName, context));
                    */

            ShareCompat.IntentBuilder
                    .from((Activity)context) // getActivity() or activity field if within Fragment
                    .setText(body)
                    .setSubject(subject)
                    .setType("text/plain") // most general text sharing MIME type
                    .startChooser();
        }
        catch(ActivityNotFoundException e){
            System.out.println("Exception raises during sending mail. Discription: " + e);
            Toast.makeText(context, R.string.email_activity_not_found_exception_text,
                    Toast.LENGTH_SHORT).show();
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
            if (mProducts == null || mProducts.size() == 0) return "";
        }

        String result = "";
        String divider = context.getString(R.string.divider);
        String productDivider = context.getString(R.string.product_divider);
        boolean firstLine = true;
        for (IListItem product: mProducts) {
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
        Cursor data = contentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        assert data != null;
        int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT);
        int keyIsCheckedIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_CHECKED);

        // Читаем данные из базы
        while (data.moveToNext()){
            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    data.getFloat(keyCountIndex), data.getInt(keyIsCheckedIndex) != 0);
            mProducts.add(newProduct);
        }

        // Закроем курсор
        data.close();
    }

    @SuppressWarnings("unused")
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
        jsonList.put(SL_ContentProvider.KEY_ID,   getId());
        jsonList.put(SL_ContentProvider.KEY_NAME, getName());
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

        for (IListItem product: mProducts) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put(SL_ContentProvider.KEY_PRODUCT_ID,  product.getId());
            jsonItem.put(SL_ContentProvider.KEY_NAME,        product.getName());
            jsonItem.put(SL_ContentProvider.KEY_COUNT,       product.getCount());
            jsonItem.put(SL_ContentProvider.KEY_IS_CHECKED,  product.isChecked());

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }
        return array;
    }

    private JSONArray getJSONArrayFromDB(Context context) throws JSONException{
        JSONArray array = new JSONArray();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        assert data != null;
        int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT);
        int keyIsCheckedIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_CHECKED);

        // Читаем данные из базы и записываем в объект JSON
        while (data.moveToNext()){
            JSONObject jsonItem = new JSONObject();

            jsonItem.put(SL_ContentProvider.KEY_PRODUCT_ID, data.getString(keyIdIndex));
            jsonItem.put(SL_ContentProvider.KEY_NAME, data.getString(keyNameIndex));
            jsonItem.put(SL_ContentProvider.KEY_COUNT, data.getString(keyCountIndex));
            jsonItem.put(SL_ContentProvider.KEY_IS_CHECKED,  data.getInt(keyIsCheckedIndex) != 0);

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }

        // Закроем курсор
        data.close();

        return array;
    }

    private void createCachedFile(Context context, String fileName, String content) throws IOException {

        File cacheFile = new File(context.getCacheDir() + File.separator + fileName);

        FileOutputStream fos = new FileOutputStream(cacheFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);

        pw.flush();
        pw.close();
    }

    public void loadProductsFromString(Context context, String stringOfProducts) {
        String divider = context.getString(R.string.divider);
        String productDivider = context.getString(R.string.product_divider);

        // Преобразуем строку в массив подстрок - продуктов по разделителю productDivider
        String[] array = stringOfProducts.split(productDivider);
        for (String strProduct : array) {
            // Уберем пробелы и знаки переноса в начале и в конце строки
            strProduct = strProduct.trim();
            // Пустую строку пропускаем
            if (strProduct.isEmpty()) continue;

            // Преобразуем строку-продукт в массив подстрок по разделителю divider
            String[] productArray = strProduct.split(divider);

            // Первым элементом в массиве productArray всегда будет Name
            String name = productArray[0].trim();

            // Если имя не заполнено, то продукт пропускаем
            if (name.isEmpty()) continue;

            float count = 1;
            // Если элементов в массиве больше 2 или равно 1,
            // значит количество введено некорректно и приравнивается 1.
            if (productArray.length == 2){
                // Попытаемся преобразовать строку в float
                try {
                    count = Float.parseFloat(productArray[1]);
                }catch (Exception e){
                    Log.i("LOAD_PRODUCT", e.getMessage());
                }
            }

            // Если количество отрицательное, приравниваем его значению по умолчанию
            if (count < 0) count = 1;

            // Создаем объект и добавляем в массив продуктов списка
            Product product = new Product(-1, name, count);
            addProduct(product);
        }
    }

    public void startInShopActivity(Context context){
        Intent intent = new Intent(context, ShoppingListInShopActivity.class);
        intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue(), this);
        context.startActivity(intent);
    }

    public void startEditingActivity(Context context){
        Intent intent = new Intent(context, ShoppingListEditingActivity.class);
        intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue(), this);
        if (isNew) intent.putExtra(EXTRAS_KEYS.PRODUCTS.getValue(), getProducts());
        context.startActivity(intent);
    }

    public void startLoadShoppingListActivity(Context context){
        Intent intent = new Intent(context, LoadShoppingListActivity.class);
        intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue() , this);
        context.startActivity(intent);
    }

    public void startOpeningOptionChoiceActivity(Context context){
        Intent intent = new Intent(context, OpeningOptionChoiceActivity.class);
        intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue() , this);
        context.startActivity(intent);
    }
}

package com.RightDirection.ShoppingList.items;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.LoadShoppingListActivity;
import com.RightDirection.ShoppingList.activities.OpeningOptionChoiceActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.contentProvider;

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

    public ShoppingList(Cursor data){
        super(data.getLong(data.getColumnIndexOrThrow(contentProvider.KEY_ID)),
                data.getString(data.getColumnIndexOrThrow(contentProvider.KEY_NAME)));
    }

    protected ShoppingList(Parcel in) {
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private void addProduct(Product product){
        if (mProducts == null) mProducts = new ArrayList<>();

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
        contentValues.put(contentProvider.KEY_NAME, getName());
        Uri insertedId = contentResolver.insert(contentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));

        contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

        if (mProducts == null) return;

        // Запишем составлящие списка покупок в базу данных
        for (Product item : mProducts) {
            contentValues.put(contentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(contentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(contentProvider.KEY_COUNT, item.getCount());
            contentValues.put(contentProvider.KEY_IS_CHECKED, item.isChecked());
            contentResolver.insert(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }

        // Список покупок более не новый
        isNew = false;
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(contentProvider.SHOPPING_LISTS_CONTENT_URI,
                contentProvider.KEY_ID + "=" + getId(), null);
    }

    @Override
    public void updateInDB(Context context) {
        if (mProducts == null) return;

        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Сначала удалим все записи редактируемого списка покупок из БД
        contentResolver.delete(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                contentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null);

        // Запишем составлящие списка покупок в базу данных
        for (ListItem item: mProducts) {
            contentValues.put(contentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(contentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(contentProvider.KEY_COUNT, item.getCount());
            contentValues.put(contentProvider.KEY_IS_CHECKED, item.isChecked());
            contentResolver.insert(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
        }
    }

    public void updateCheckedInDB(Context context) {
        if (mProducts == null) return;

        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        for (ListItem item: mProducts) {
            contentValues.put(contentProvider.KEY_IS_CHECKED, item.isChecked());
            contentResolver.update(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues,
                    contentProvider.KEY_SHOPPING_LIST_ID + "=" + getId() + " AND "
                            + contentProvider.KEY_PRODUCT_ID + "=" + item.getId() + " AND "
                            + contentProvider.KEY_COUNT + "=" + item.getCount(), null);
        }
    }

    public void removeCheckedFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        for (ListItem item: mProducts) {
            if (item.isChecked()) {
                contentResolver.delete(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                        contentProvider.KEY_SHOPPING_LIST_ID + "=" + getId() + " AND "
                                + contentProvider.KEY_PRODUCT_ID + "=" + item.getId() + " AND "
                                + contentProvider.KEY_COUNT + "=" + item.getCount(), null);
            }
        }

        // Удалим продукты из массива
        for (int i = mProducts.size()-1; i >= 0; i--) {
            Product product = mProducts.get(i);
            if (product.isChecked) mProducts.remove(i);
        }
    }

    @Override
    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(contentProvider.KEY_NAME, getName());
        contentResolver.update(contentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, contentProvider.KEY_ID +  " = " + getId(), null);
    }

    public void addNotExistingProductsToDB(Context context) {
        if (mProducts == null || mProducts.size() == 0) return;

        // Создадим строку условия
        String where = getWhereConditionForName();

        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(contentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        ArrayList<String> foundProducts = new ArrayList<>();
        int keyNameIndex = data.getColumnIndexOrThrow(contentProvider.KEY_NAME);
        while (data.moveToNext()){
            foundProducts.add(data.getString(keyNameIndex));
        }
        data.close();

        // Добавим несуществующие продукты в базу данных
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < mProducts.size(); i++) {
            String name = mProducts.get(i).getName();
            if (!foundProducts.contains(name)){
                contentValues.put(contentProvider.KEY_NAME, name);
                contentResolver.insert(contentProvider.PRODUCTS_CONTENT_URI, contentValues);
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
        Cursor data = contentResolver.query(contentProvider.PRODUCTS_CONTENT_URI, null,
                where, null, null);

        // Создадим массив с найденными именами продуктов
        HashMap<String, Long> map = new HashMap<>(data.getCount());
        int keyIdIndex = data.getColumnIndexOrThrow(contentProvider.KEY_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(contentProvider.KEY_NAME);
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
        if (mProducts == null) return "";

        String where = null;
        if (mProducts.size() > 0) {
            where = contentProvider.KEY_NAME + " IN (";
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

            context.startActivity(Utils.getSendEmailIntent("",
                    context.getString(R.string.json_file_identifier) + " '" + getName() + "'",
                    convertShoppingListToString(context), fileName));
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
        Cursor data = contentResolver.query(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                contentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        int keyIdIndex = data.getColumnIndexOrThrow(contentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(contentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(contentProvider.KEY_COUNT);
        int keyIsCheckedIndex = data.getColumnIndexOrThrow(contentProvider.KEY_IS_CHECKED);

        // Читаем данные из базы
        while (data.moveToNext()){
            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    null, data.getFloat(keyCountIndex), data.getInt(keyIsCheckedIndex) != 0);
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
        jsonList.put(contentProvider.KEY_ID,   getId());
        jsonList.put(contentProvider.KEY_NAME, getName());
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
            jsonItem.put(contentProvider.KEY_PRODUCT_ID,  product.getId());
            jsonItem.put(contentProvider.KEY_NAME,        product.getName());
            jsonItem.put(contentProvider.KEY_COUNT,       product.getCount());
            jsonItem.put(contentProvider.KEY_IS_CHECKED,  product.isChecked());

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }
        return array;
    }

    private JSONArray getJSONArrayFromDB(Context context) throws JSONException{
        JSONArray array = new JSONArray();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                contentProvider.KEY_SHOPPING_LIST_ID + "=" + getId(), null ,null);

        // Определим индексы колонок для считывания
        int keyIdIndex = data.getColumnIndexOrThrow(contentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(contentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(contentProvider.KEY_COUNT);
        int keyIsCheckedIndex = data.getColumnIndexOrThrow(contentProvider.KEY_IS_CHECKED);

        // Читаем данные из базы и записываем в объект JSON
        while (data.moveToNext()){
            JSONObject jsonItem = new JSONObject();

            jsonItem.put(contentProvider.KEY_PRODUCT_ID, data.getString(keyIdIndex));
            jsonItem.put(contentProvider.KEY_NAME, data.getString(keyNameIndex));
            jsonItem.put(contentProvider.KEY_COUNT, data.getString(keyCountIndex));
            jsonItem.put(contentProvider.KEY_IS_CHECKED,  data.getInt(keyIsCheckedIndex) != 0);

            // Добавим объект JSON в массив
            array.put(jsonItem);
        }

        // Закроем курсор
        data.close();

        return array;
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
            Product product = new Product(-1, name, null, count);
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

package com.RightDirection.ShoppingList.models;

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
import com.RightDirection.ShoppingList.activities.ChooseRecipientActivity;
import com.RightDirection.ShoppingList.activities.LoadShoppingListActivity;
import com.RightDirection.ShoppingList.activities.OpeningOptionChoiceActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

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

    private static final String TAG = "ShoppingListPOJO";
    private ArrayList<IListItem> mProducts;
    private boolean isFiltered;

    // Переменные заполняются при чтении инфо о списках покупок из БД. При этом сами продукты не загружаются (например, в MainActivity)
    private int totalCountOfProducts = 0;
    private int numberOfCrossedOutProducts = 0;
    private float totalSum = 0;
    private float leftToBuyOn = 0;

    public ShoppingList(long id, String name) {
        super(id, name);
        isFiltered = false;
    }

    public ShoppingList(long id, String name, ArrayList<IListItem> products) {
        super(id, name);
        mProducts = products;
        isFiltered = false;
    }

    public ShoppingList(Cursor data){
        super(data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_ID)),
                data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME)));
        try {
            isFiltered = data.getInt(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_FILTERED)) != 0;
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            isFiltered = false;
        }
        try {
            totalCountOfProducts = data.getInt(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_TOTAL_COUNT));
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            totalCountOfProducts = 0;
        }
        try {
            numberOfCrossedOutProducts = data.getInt(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NUMBER_OF_CROSSED_OUT));
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            numberOfCrossedOutProducts = 0;
        }
        try {
            totalSum = data.getFloat(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_TOTAL_SUM));
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            totalSum = 0;
        }
        try {
            leftToBuyOn = data.getFloat(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_LEFT_TO_BUY_ON));
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            leftToBuyOn = 0;
        }
    }

    private ShoppingList(Parcel in) {
        super(in);
        isFiltered = in.readByte() != 0;
    }

    public float getTotalSum() {
        return totalSum;
    }

    public float getLeftToBuyOn() {
        return leftToBuyOn;
    }

    public int getTotalCountOfProducts() {
        return totalCountOfProducts;
    }

    public int getNumberOfCrossedOutProducts() {
        return numberOfCrossedOutProducts;
    }

    public void setTotalCountOfProducts(int _totalCountOfProducts) {
        totalCountOfProducts = _totalCountOfProducts;
    }

    public void setNumberOfCrossedOutProducts(int _numberOfCrossedOutProducts) {
        numberOfCrossedOutProducts = _numberOfCrossedOutProducts;
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
        dest.writeByte((byte) (isFiltered ? 1 : 0));
    }

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

    public boolean isFiltered() {
        return isFiltered;
    }

    public void setFiltered(boolean filtered) {
        isFiltered = filtered;
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
        writeShoppingListProductsToDB(context, contentResolver, contentValues);

        // Список покупок более не новый
        isNew = false;
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                SL_ContentProvider.KEY_ID + "= ?", new String[]{String.valueOf(getId())});
    }

    @Override
    public void updateInDB(Context context) {
        if (mProducts == null) return;

        // Обновим текущий список покупок
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Сначала удалим все записи редактируемого списка покупок из БД
        contentResolver.delete(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "= ?", new String[]{String.valueOf(getId())});

        // Запишем составлящие списка покупок в базу данных
        writeShoppingListProductsToDB(context, contentResolver, contentValues);
    }

    private void writeShoppingListProductsToDB(Context context, ContentResolver contentResolver, ContentValues contentValues) {
        for (IListItem item: mProducts) {
            contentValues.put(SL_ContentProvider.KEY_SHOPPING_LIST_ID, getId());
            contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, item.getId());
            contentValues.put(SL_ContentProvider.KEY_COUNT, item.getCount());
            contentValues.put(SL_ContentProvider.KEY_IS_CHECKED, item.isChecked());
            Product product = (Product)item;
            contentValues.put(SL_ContentProvider.KEY_PRICE, product.getCurrentPrice());
            Unit currentUnit = product.getCurrentUnit();
            if (currentUnit != null && product.getCurrentUnit().getId() != Utils.EMPTY_ID) {
                contentValues.put(SL_ContentProvider.KEY_UNIT_ID, product.getCurrentUnit().getId());
            }
            contentResolver.insert(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);

            // Обновим цену и ед. измерения по умолчанию
            boolean needToUpdate = false;
            if (product.getCurrentPrice() != Product.EMPTY_CURRENT_PRICE && product.getCurrentPrice() != product.getLastPrice()){
                product.setLastPrice(product.getCurrentPrice());
                needToUpdate = true;
            }
            if (currentUnit != null && product.getCurrentUnit().getId() != Utils.EMPTY_ID && currentUnit != product.getDefaultUnit()) {
                product.setDefaultUnit(currentUnit);
                needToUpdate = true;
            }
            if (needToUpdate) {
                product.updateInDB(context);
            }
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
                        SL_ContentProvider.KEY_SHOPPING_LIST_ID + "= ? AND " + SL_ContentProvider.KEY_ID + "= ?",
                        new String[]{String.valueOf(getId()), String.valueOf(product.getRowId())});
            }
        }
    }

    public void removeCheckedFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        for (IListItem item: mProducts) {
            if (item instanceof Product && item.isChecked()) {
                Product product = (Product)item;
                contentResolver.delete(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                        SL_ContentProvider.KEY_SHOPPING_LIST_ID + "= ? AND "
                                + SL_ContentProvider.KEY_ID + "= ?",
                        new String[]{String.valueOf(getId()), String.valueOf(product.getRowId())});
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
                values, SL_ContentProvider.KEY_ID +  " = ?", new String[]{String.valueOf(getId())});
    }

    public void saveFilteredValueInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(SL_ContentProvider.KEY_IS_FILTERED, isFiltered());
        contentResolver.update(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, SL_ContentProvider.KEY_ID +  " = ?", new String[]{String.valueOf(getId())});
    }

    public void addNotExistingProductsToDBandSetId(Context context) {
        if (mProducts == null || mProducts.size() == 0) return;

        // Создадим строку условия
        String where = getWhere(SL_ContentProvider.KEY_NAME);
        // Создадим строку аргументов
        String[] whereArgs = getWhereArgs(SL_ContentProvider.KEY_NAME, context);

        // Произведем выборку из базы данных существующих продуктов
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI, null,
                where, whereArgs, null);

        // Создадим массив с найденными именами продуктов
        if (data != null) {
            HashMap<String, Product> foundProducts = new HashMap<>(data.getCount());
            int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
            int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
            while (data.moveToNext()) {
                foundProducts.put(data.getString(keyNameIndex), new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex)));
            }
            data.close();

            // Добавим несуществующие продукты в базу данных. Присвоим корректные id всем товарам
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < mProducts.size(); i++) {
                Product currentProduct = (Product) mProducts.get(i);
                Product productFromDB = foundProducts.get(currentProduct.getName());
                if (productFromDB == null) {
                    // Товар не найден в базе данных, и его необходимо добавить
                    currentProduct.addToDB(context);
                }else{
                    // Товар найден в базе данных, установим корректный id для него.
                    currentProduct.setId(productFromDB.getId());
                }
            }
        }
    }

    private void addNotExistingUnitsToDBandSetId(Context context) {
        if (mProducts == null || mProducts.size() == 0) return;

        String where = getWhere(SL_ContentProvider.KEY_UNIT_SHORT_NAME);
        String[] whereArgs = getWhereArgs(SL_ContentProvider.KEY_UNIT_SHORT_NAME, context);

        // Произведем выборку из базы данных существующих ед. измерения
        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.UNITS_CONTENT_URI, null,
                where, whereArgs, null);

        // Создадим массив с найденными ед. измерения
        if  (data != null) {
            HashMap<String, Unit> foundUnits = new HashMap<>(data.getCount());
            int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_ID);
            int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_NAME);
            int keyShortNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_SHORT_NAME);
            while (data.moveToNext()) {
                foundUnits.put(data.getString(keyShortNameIndex), new Unit(data.getLong(keyIdIndex), data.getString(keyNameIndex), data.getString(keyShortNameIndex)));
            }
            data.close();

            // Добавим несуществующие ед. измерения в базу данных. Присвоим корректные id всем еденицам измерения
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < mProducts.size(); i++) {
                Product product = (Product) mProducts.get(i);
                Unit currentUnit = product.getCurrentUnit();
                if (currentUnit == null) { continue; }

                Unit unitFromDB = foundUnits.get(product.getUnitShortName(context));
                if (unitFromDB == null) {
                    // Ед. измерения не найдена в базе данных, и ее необходимо добавить
                    contentValues.put(SL_ContentProvider.KEY_UNIT_NAME, currentUnit.getName());
                    contentValues.put(SL_ContentProvider.KEY_UNIT_SHORT_NAME, currentUnit.getShortName());
                    Uri insertedId = contentResolver.insert(SL_ContentProvider.UNITS_CONTENT_URI, contentValues);
                    currentUnit.setId(ContentUris.parseId(insertedId));
                }else{
                    // Ед. измерения найдена в базе данных, установим корректный id для ед. измерения продукта.
                    currentUnit.setId(unitFromDB.getId());
                }
            }
        }
    }

    private String getWhere(String key) {
        if (mProducts == null) return null;

        String where = null;
        if (mProducts.size() > 0) {
            where = key + " IN (";
            where += "?"; // первый раз без запятой в начале
            for (int i = 1; i < mProducts.size(); i++) {
                where += ",?";
            }
            where += ")";
        }
        return where;
    }

    private String[] getWhereArgs(String key, Context context) {
        if (mProducts == null || mProducts.size() == 0) return null;

        String[] where = new String[mProducts.size()];
        for (int i = 0; i < mProducts.size(); i++) {
            if (key.equals(SL_ContentProvider.KEY_NAME)) {
                where[i] = mProducts.get(i).getName();
            }else if (key.equals(SL_ContentProvider.KEY_UNIT_SHORT_NAME)){
                Product product = (Product)mProducts.get(i);
                where[i] = product.getUnitShortName(context);
            }
        }
        return where;
    }

    public void share(Context context){

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

    public String convertShoppingListToString(Context context){
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
        for (IListItem item: mProducts) {
            if (!firstLine) result = result + "\n";
            else firstLine = false;

            Product product = (Product)item;
            result = result + product.getName()
                    + divider + " " + String.valueOf(product.getCount())
                    + divider + " " + String.valueOf(product.getUnitShortName(context))
                    + divider + " " + String.valueOf(product.getPrice())
                    + productDivider;
        }

        return result;
    }

    private void getProductsFromDB(Context context) {
        if (mProducts != null) mProducts.clear();
        else mProducts = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "= ?", new String[]{String.valueOf(getId())} ,null);

        // Определим индексы колонок для считывания
        if (data != null) {
            int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
            int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
            int keyCountIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT);
            int keyIsCheckedIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_CHECKED);

            // Читаем данные из базы
            while (data.moveToNext()) {
                Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                        data.getFloat(keyCountIndex), data.getInt(keyIsCheckedIndex) != 0);
                mProducts.add(newProduct);
            }

            // Закроем курсор
            data.close();
        }
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
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "= ?", new String[]{String.valueOf(getId())} ,null);

        // Определим индексы колонок для считывания
        if (data != null) {
            int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
            int keyNameIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
            int keyCountIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT);
            int keyIsCheckedIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_CHECKED);

            // Читаем данные из базы и записываем в объект JSON
            while (data.moveToNext()) {
                JSONObject jsonItem = new JSONObject();

                jsonItem.put(SL_ContentProvider.KEY_PRODUCT_ID, data.getString(keyIdIndex));
                jsonItem.put(SL_ContentProvider.KEY_NAME, data.getString(keyNameIndex));
                jsonItem.put(SL_ContentProvider.KEY_COUNT, data.getString(keyCountIndex));
                jsonItem.put(SL_ContentProvider.KEY_IS_CHECKED, data.getInt(keyIsCheckedIndex) != 0);

                // Добавим объект JSON в массив
                array.put(jsonItem);
            }

            // Закроем курсор
            data.close();
        }

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

            // Создаем объект и добавляем в массив продуктов списка
            Product product = new Product(
                    Utils.EMPTY_ID,
                    name,
                    getCountFromArray(productArray),
                    getPriceFromArray(productArray),
                    getUnitFromArray(productArray, context));
            addProduct(product);
        }

        // Сначала нужно добавить новые продукты из списка в базу данных.
        // Синхронизацияя должна производиться по полю Name
        addNotExistingProductsToDBandSetId(context);

        // Также необходимо добавить в базу ед. измерения. Синхронизация по краткому наименованию/
        // В этом же методе присваиваем правильные id единицам измерения из списка.
        addNotExistingUnitsToDBandSetId(context);
    }

    private float getPriceFromArray(String[] productArray){
        float price = Product.EMPTY_CURRENT_PRICE;
        if (productArray.length >= 4){
            // Попытаемся преобразовать строку в float
            try {
                price = Float.parseFloat(productArray[3]);
                if (price < 0) price = Product.EMPTY_CURRENT_PRICE;
            }catch (Exception e){
                Log.i("LOAD_PRODUCT", e.getMessage());
            }
        }

        return price;
    }

    private Unit getUnitFromArray(String[] productArray, Context context){
        String unitShortName = null;
        if (productArray.length >= 3){
            unitShortName = productArray[2];
        }

        // Создаем "техническую" ед. измерения, которой в дальнейшем будет назначено корректное id и имя
        if (unitShortName != null && !unitShortName.equals("") && !unitShortName.equals(context.getString(R.string.default_unit))) {
            return new Unit(Utils.EMPTY_ID, unitShortName, unitShortName);
        }else{
            return null;
        }
    }

    private float getCountFromArray(String[] productArray){
        float count = 1;
        // Если элементов в массиве меньше 2,
        // значит количество введено некорректно и приравнивается 1.
        if (productArray.length >= 2){
            // Попытаемся преобразовать строку в float
            try {
                count = Float.parseFloat(productArray[1]);
                if (count < 0) count = 1;
            }catch (Exception e){
                Log.i("LOAD_PRODUCT", e.getMessage());
            }
        }

        return count;
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

    public boolean openChooseRecipientActivity(Context context) {
        if (FirebaseUtil.userSignedIn(context)) {
            Intent intent = new Intent(context, ChooseRecipientActivity.class);
            intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue(), this);
            intent.putExtra(EXTRAS_KEYS.PRODUCTS.getValue(), getProducts());
            context.startActivity(intent);
            return true;
        }else{
            Toast.makeText(context, R.string.log_in_suggestion, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

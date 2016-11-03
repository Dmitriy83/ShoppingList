package com.RightDirection.ShoppingList.utils;

//Класс с глобальными константами и методами

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.items.ListItem;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.items.ShoppingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс с глобальными константами и методами
 */
public class Utils {
    public static final int NEED_TO_UPDATE = 1;
    public static final int GET_CATEGORY = 2;

    private static Utils ourInstance = new Utils();

    public static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {}

    public static void createShoppingListJSONFile(Context context, ShoppingList shoppingList, String fileName) throws JSONException {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor data = contentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, null,
                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + shoppingList.getId(), null ,null);

        // Определим индексы колонок для считывания
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);

        // Читаем данные из базы и записываем в объект JSON
        JSONArray listItemsArray = new JSONArray();
        while (data.moveToNext()){
            JSONObject listItem = new JSONObject();

            listItem.put(ShoppingListContentProvider.KEY_PRODUCT_ID, data.getString(keyIdIndex));
            listItem.put(ShoppingListContentProvider.KEY_NAME, data.getString(keyNameIndex));
            listItem.put(ShoppingListContentProvider.KEY_COUNT, data.getString(keyCountIndex));

            // Добавим объект JSON в массив
            listItemsArray.put(listItem);
        }

        JSONObject shoppingListJSON = new JSONObject();
        shoppingListJSON.put(ShoppingListContentProvider.KEY_PRODUCT_ID,    shoppingList.getId());
        shoppingListJSON.put(ShoppingListContentProvider.KEY_NAME,          shoppingList.getName());
        shoppingListJSON.put("items",   listItemsArray);

        String jsonStr = shoppingListJSON.toString();
        Log.i("CREATING_JSON", jsonStr);

        data.close();

        // Запишем текст в файл
        try {
            Utils.createCachedFile(context, fileName, jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getListNameFromJSON(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        return json.getString(ShoppingListContentProvider.KEY_NAME);
    }

    public static ArrayList<Product> getProductsFromJSON(String jsonStr) throws JSONException {
        ArrayList<Product> result = new ArrayList<>();

        JSONObject jObject = new JSONObject(jsonStr);
        JSONArray jArray = jObject.getJSONArray("items");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jItem = jArray.getJSONObject(i);

            long itemId = jItem.getLong(ShoppingListContentProvider.KEY_PRODUCT_ID);
            String itemName = jItem.getString(ShoppingListContentProvider.KEY_NAME);

            float count = 1;
            try {
                String itemCount = jItem.getString(ShoppingListContentProvider.KEY_COUNT);
                if (!itemCount.isEmpty()) {
                    count = Float.parseFloat(itemCount);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            result.add(new Product(itemId, itemName, null, count));
        }

        return result;
    }

    public static void createCachedFile(Context context, String fileName, String content) throws IOException {

        File cacheFile = new File(context.getCacheDir() + File.separator + fileName);
        cacheFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(cacheFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);

        pw.flush();
        pw.close();
    }

    public static Intent getSendEmailIntent(@Nullable String email, String subject, String body, String fileName) {

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        //Explicitly only use Gmail to send
        //emailIntent.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");

        emailIntent.setType("plain/text");

        //Add the recipients
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        //Add the attachment by specifying a reference to our custom ContentProvider
        //and the specific file of interest
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/files/" + fileName));

        return emailIntent;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
        String ret = convertStreamToString(fileInputStream);
        fileInputStream.close();
        return ret;
    }

    public static ArrayList<Product> sortArrayListByCategories(ArrayList<Product> arrayList){
        // Сначала отсортируем список
        Collections.sort(arrayList, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                Category lCategory = lhs.getCategory();
                Category rCategory = rhs.getCategory();

                // Элементы с категорией равной Null должны быть вверху списка
                if (lCategory == null && rCategory != null) {
                    return -1;
                } else if (rCategory == null && lCategory != null) {
                    return 1;
                }

                if (lCategory != null && rCategory != null && !lCategory.equals(rCategory)) {
                    if (lCategory.getOrder() != rCategory.getOrder()) {
                        Integer lOrder = lCategory.getOrder();
                        return lOrder.compareTo(rCategory.getOrder());
                    }

                    // Элементы с категорией с именем равным Null должны быть выше
                    if (lCategory.getName() == null && rCategory.getName() != null) {
                        return -1;
                    } else if (rCategory.getName() == null && lCategory.getName() != null) {
                        return 1;
                    }

                    if (lCategory.getName() != null && !lCategory.getName().equals(rCategory.getName())) {
                        return String.CASE_INSENSITIVE_ORDER.compare(lCategory.getName(),
                                rCategory.getName());
                    }
                }

                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });

        return arrayList;
    }

    public static ArrayList addCategoriesInArrayListOfProducts(Context context, ArrayList arrayList){
        // Если элемент только один, то добавлять ничего не надо
        if (arrayList.size() == 1) return arrayList;

        Product product, prevProduct;
        Category category, prevCategory;
        Category emptyCategory = new Category(-1, context.getString(R.string.category_not_assigned), 0);
        for (int i = arrayList.size() - 2; i >= 0; i--) {
            product = (Product)arrayList.get(i);
            category = product.getCategory();
            if (category == null || category.getName() == null) category = emptyCategory;
            prevProduct = (Product)arrayList.get(i + 1);
            prevCategory = prevProduct.getCategory();
            if (prevCategory == null || prevCategory.getName() == null) prevCategory = emptyCategory;

            // Сравниваем по именам, т.к. указатели на ссылки разные даже для одинаковых категорий
            if (!category.getName().equals(prevCategory.getName())){
                // Добавим элемент-категорию (разделитель групп)
                arrayList.add(i + 1, prevCategory);
            }

            if (i == 0) arrayList.add(0, category);
        }

        return arrayList;
    }

    public static ArrayList removeCategoriesFromArrayListOfProducts(ArrayList arrayList){
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            ListItem item = (ListItem)arrayList.get(i);
            if (item instanceof Category) arrayList.remove(i);
        }
        return arrayList;
    }

}

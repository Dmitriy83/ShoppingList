package com.RightDirection.ShoppingList.utils;

//Класс с глобальными константами и методами

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс с глобальными константами и методами
 */
public class Utils {
    public static final int NEED_TO_UPDATE = 1;
    public static final int GET_CATEGORY = 2;
    public static final int GET_CATEGORY_IMAGE = 3;
    public static final int TIMEOUT = 15000;

    public static String getListNameFromJSON(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        return json.getString(SL_ContentProvider.KEY_NAME);
    }

    public static ArrayList<IListItem> getProductsFromJSON(String jsonStr) throws JSONException {
        ArrayList<IListItem> result = new ArrayList<>();

        JSONObject jObject = new JSONObject(jsonStr);
        JSONArray jArray = jObject.getJSONArray("items");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jItem = jArray.getJSONObject(i);

            long itemId = jItem.getLong(SL_ContentProvider.KEY_PRODUCT_ID);
            String itemName = jItem.getString(SL_ContentProvider.KEY_NAME);

            float count = 1;
            try {
                String itemCount = jItem.getString(SL_ContentProvider.KEY_COUNT);
                if (!itemCount.isEmpty()) {
                    count = Float.parseFloat(itemCount);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            result.add(new Product(itemId, itemName, count));
        }

        return result;
    }

    private static String convertStreamToString(InputStream is) throws Exception {
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

    public static void sortArrayListByCategories(ArrayList<IListItem> arrayList){
        // Сначала отсортируем список
        Collections.sort(arrayList, new Comparator<IListItem>() {
            @Override
            public int compare(IListItem lhs, IListItem rhs) {
                if (!(lhs instanceof Product && rhs instanceof Product)) return 0;

                Product lProduct = (Product) lhs;
                Product rProduct = (Product) rhs;

                Category lCategory = lProduct.getCategory();
                Category rCategory = rProduct.getCategory();

                // Элементы с категорией равной Null должны быть вверху списка
                if (lCategory == null && rCategory != null) {
                    return -1;
                } else if (rCategory == null && lCategory != null) {
                    return 1;
                }

                if (lCategory != null && !lCategory.equals(rCategory)) {
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
    }

    public static void addCategoriesInArrayListOfProducts(Context context, ArrayList<IListItem> arrayList){
        // Если элемент только один, то добавлять ничего не надо
        if (arrayList.size() == 1) return;

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
    }

    public static void removeCategoriesFromArrayListOfProducts(ArrayList<IListItem> arrayList){
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            IListItem item = arrayList.get(i);
            if (item instanceof Category) arrayList.remove(i);
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static Intent getSendEmailIntent(@Nullable String recipientEmail, String subject, String body, @SuppressWarnings("SameParameterValue") String fileName) {

        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

        //Explicitly  use Gmail to send
        //emailIntent.seonlytClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");

        emailIntent.setType("plain/text");

        //Add the recipients
        emailIntent.setData(Uri.parse("mailto:" + recipientEmail));
        //emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipientEmail });

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        if (fileName != null) {
            //Add the attachment by specifying a reference to our custom ContentProvider
            //and the specific file of interest
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://com.RightDirection.shoppinglistcontentprovider/files/" + fileName));
        }

        //return Intent.createChooser(emailIntent, context.getString(R.string.choose_email_client));
        return emailIntent;
    }


    public static boolean showHelpInShop(Context context) {
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPref.getBoolean(context.getApplicationContext().getString(R.string.pref_key_show_help_screens), true);
    }

    public static boolean showHelpMainActivity(Context context) {
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPref.getBoolean(context.getString(R.string.pref_key_show_help_in_main_activity), true);
    }

    public static boolean showChooseModeDialog(Context context) {
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPref.getBoolean(context.getApplicationContext().getString(R.string.pref_key_show_activity_opening_option_choice), true);
    }
}

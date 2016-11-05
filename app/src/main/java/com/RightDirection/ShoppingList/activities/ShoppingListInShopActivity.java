package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterShoppingListInShop;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.utils.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

import java.util.ArrayList;

public class ShoppingListInShopActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList mProducts;
    private ListAdapterShoppingListInShop mProductsAdapter;
    private long mListId;
    private String mListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shopping_list_in_shop);

        // Установка доработанного меню
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //setContentView(R.layout.activity_shopping_list_in_shop);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

        // Получим значения из переданных параметров родительской активности
        Intent sourceIntent = getIntent();
        mListId = sourceIntent.getLongExtra(String.valueOf(R.string.list_id), -1);

        // Установим заголовок активности
        mListName = sourceIntent.getStringExtra(String.valueOf(R.string.list_name));
        if (mListName != null) {
            setTitle(mListName);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rvProducts);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Создаем массив для хранения списка покупок
        if (savedInstanceState == null) {
            mProducts = new ArrayList<>();
        }
        else{
            mProducts = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.shopping_list_items));
        }

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showImages = sharedPref.getBoolean(getApplicationContext().getString(R.string.pref_key_show_images), true);
        int listItemLayout = R.layout.list_item_shopping_list_in_shop;
        if (!showImages) listItemLayout = R.layout.list_item_shopping_list_in_shop_without_image;
        // Создадим новый адаптер для работы со списком покупок
        mProductsAdapter = new ListAdapterShoppingListInShop(this, listItemLayout, mProducts);

        // Привяжем адаптер к фрагменту-списку
        recyclerView.setAdapter(mProductsAdapter);

        if (savedInstanceState == null) {
            // Заполним список покупок из базы данных
            getLoaderManager().initLoader(0, null, this);
        }
        else{
            mProductsAdapter.setIsFiltered(savedInstanceState.getBoolean(String.valueOf(R.string.is_filtered)));
            ArrayList<Product> originalValues = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.shopping_list_items_original_values));
            mProductsAdapter.setOriginalValues(originalValues);
        }

        // Откроем подсказку, если необходимо
        boolean showHelp = sharedPref.getBoolean(getApplicationContext().getString(R.string.pref_key_show_help_screens), true);
        if (showHelp){
            Intent intent = new Intent(this, HelpShoppingListInShopActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        outState.putParcelableArrayList(String.valueOf(R.string.shopping_list_items), mProducts);
        outState.putParcelableArrayList(String.valueOf(R.string.shopping_list_items_original_values), mProductsAdapter.getOriginalValues());
        outState.putBoolean(String.valueOf(R.string.is_filtered), mProductsAdapter.isFiltered());

        super.onSaveInstanceState(outState);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListId, null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyCountIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);
        int keyCategoryIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_ID);
        int keyCategoryNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_NAME);
        int keyCategoryOrderIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_ORDER);

        mProducts.clear();
        while (data.moveToNext()){
            Category category = new Category(data.getLong(keyCategoryIndex), data.getString(keyCategoryNameIndex),
                    data.getInt(keyCategoryOrderIndex));

            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    ShoppingListContentProvider.getImageUri(data), data.getFloat(keyCountIndex), category);
            mProducts.add(newProduct);
        }

        mProducts = Utils.sortArrayListByCategories(mProducts);
        mProducts = Utils.addCategoriesInArrayListOfProducts(this, mProducts);
        mProductsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_shopping_list_in_shop_menu, menu);

        // Установим иконку Фильтра в нужное значение при открытии
        if (mProductsAdapter.isFiltered()) {
            setFilterItemSelected(menu.getItem(1));
        }
        else{
            setFilterItemUnselected(menu.getItem(1));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработаем нажатие на элемент подменю.
        int id = item.getItemId();
        View view = findViewById(android.R.id.content);
        if (view == null) return super.onOptionsItemSelected(item);

        if (id == R.id.action_filter) {
            if (mProductsAdapter.isFiltered()) {
                setFilterItemUnselected(item);
                mProductsAdapter.showMarked();
            }
            else{
                setFilterItemSelected(item);
                mProductsAdapter.hideMarked();
            }
        }
        else if (id == R.id.action_edit_shopping_list) {
            Intent intent = new Intent(this, ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_list), false);
            intent.putExtra(String.valueOf(R.string.list_id), mListId);
            if (mListName != null){
                intent.putExtra(String.valueOf(R.string.list_name), mListName);
            }
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFilterItemSelected(MenuItem menuItem){
        menuItem.setIcon(R.drawable.ic_clear_filter);
    }

    private void setFilterItemUnselected(MenuItem menuItem){
        menuItem.setIcon(R.drawable.ic_filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Utils.NEED_TO_UPDATE:
                if (resultCode == RESULT_OK) {
                    // Получим значения из переданных параметров
                    long id = data.getLongExtra(String.valueOf(R.string.item_id), 0);
                    String name = data.getStringExtra(String.valueOf(R.string.name));
                    Uri imageUri = data.getParcelableExtra(String.valueOf(R.string.item_image));
                    Category category = data.getParcelableExtra(String.valueOf(R.string.category));
                    // Обновим элемент списка (имя и картинку)
                    mProductsAdapter.updateItem(id, name, imageUri, category);
                    // Перестроим массив на случай, если изменилась категория
                    mProducts = Utils.removeCategoriesFromArrayListOfProducts(mProducts);
                    mProducts = Utils.sortArrayListByCategories(mProducts);
                    mProducts = Utils.addCategoriesInArrayListOfProducts(this, mProducts);
                    mProductsAdapter.notifyDataSetChanged();
                }
        }
    }
}

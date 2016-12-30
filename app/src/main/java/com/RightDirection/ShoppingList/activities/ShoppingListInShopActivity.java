package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.RightDirection.ShoppingList.items.ShoppingList;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.contentProvider;

import java.util.ArrayList;

public class ShoppingListInShopActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList mProducts;
    private ListAdapterShoppingListInShop mProductsAdapter;
    private ShoppingList mShoppingList;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shopping_list_in_shop);

        // Установка доработанного меню
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //setContentView(R.layout.activity_shopping_list_in_shop);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

        // Получим значения из переданных параметров родительской активности
        mShoppingList = getIntent().getParcelableExtra(String.valueOf(R.string.shopping_list));

        // На всякий случай
        if (mShoppingList == null) mShoppingList = new ShoppingList(-1, "");

        // Установим заголовок активности
        setTitle(mShoppingList.getName());

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
            mProducts = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.products));
        }

        int listItemLayout = R.layout.list_item_shopping_list_in_shop;
        if (!showImages()) listItemLayout = R.layout.list_item_shopping_list_in_shop_without_image;
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
            ArrayList<Product> originalValues = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.products_original_values));
            mProductsAdapter.setOriginalValues(originalValues);
        }

        // Откроем подсказку, если необходимо
        if (Utils.showHelpInShop(getApplicationContext()))
            startActivity(new Intent(this, HelpShoppingListInShopActivity.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        outState.putParcelableArrayList(String.valueOf(R.string.products), mProducts);
        outState.putParcelableArrayList(String.valueOf(R.string.products_original_values), mProductsAdapter.getOriginalValues());
        outState.putBoolean(String.valueOf(R.string.is_filtered), mProductsAdapter.isFiltered());

        super.onSaveInstanceState(outState);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, contentProvider.KEY_SHOPPING_LIST_ID + "=" + mShoppingList.getId(), null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mProducts.clear();
        while (data.moveToNext()){
            mProducts.add(new Product(data, new Category(data)));
        }

        Utils.sortArrayListByCategories(mProducts);
        if (showCategories()) Utils.addCategoriesInArrayListOfProducts(this, mProducts);
        mProductsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_shopping_list_in_shop_menu, menu);

        mMenu = menu;

        // Установим иконку Фильтра в нужное значение при открытии
        if (mProductsAdapter.isFiltered()) {
            setFilterItemSelected();
        }
        else{
            setFilterItemUnselected();
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
                setFilterItemUnselected();
                mProductsAdapter.showChecked();
            }
            else{
                setFilterItemSelected();
                mProductsAdapter.hideChecked();
            }
        }
        else if (id == R.id.action_edit_shopping_list) {
            saveCheckedInDB();
            Intent intent = new Intent(this, ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.shopping_list), mShoppingList);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.action_send_by_email) {
            // Создадим вспомагательный массив и удалим из него категории
            ArrayList array;
            // mProductsAdapter.getOriginalValues() может быть равен null, если фильтр еще не накладывался
            if (mProductsAdapter.getOriginalValues() == null) {
                array = new ArrayList<>(mProducts);
            }else{
                array = new ArrayList<>(mProductsAdapter.getOriginalValues());
            }
            Utils.removeCategoriesFromArrayListOfProducts(array);
            mShoppingList.setProducts(array);
            mShoppingList.sendByEmail(this);
        }
        else if (id == R.id.action_load_list) {
            Intent intentLoad = new Intent(this, LoadShoppingListActivity.class);
            intentLoad.putExtra(String.valueOf(R.string.shopping_list), mShoppingList);
            startActivity(intentLoad);
            finish();
        }
        else if (id == R.id.action_remove_checked) {
            // Сначала необходимо снять фильтр
            if (mProductsAdapter.isFiltered()) {
                setFilterItemUnselected();
                mProductsAdapter.showChecked();
            }
            // сначала заполним объект списком для изменения
            mShoppingList.setProducts(mProducts);
            // удалим вычеркнутые элементы из БД
            mShoppingList.removeCheckedFromDB(this);
            // заменим массив для фильтрации
            ArrayList<Product> originalValues = (ArrayList<Product>) mProducts.clone();
            mProductsAdapter.setOriginalValues(originalValues);
            mProductsAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFilterItemSelected(){
        MenuItem menuItem = mMenu.findItem(R.id.action_filter);
        menuItem.setIcon(R.drawable.ic_clear_filter);
    }

    private void setFilterItemUnselected(){
        MenuItem menuItem = mMenu.findItem(R.id.action_filter);
        menuItem.setIcon(R.drawable.ic_filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Utils.NEED_TO_UPDATE:
                if (resultCode == RESULT_OK) {
                    // Получим значения из переданных параметров
                    Product product = data.getParcelableExtra(String.valueOf(R.string.product));
                    // Обновим элемент списка (имя и картинку)
                    mProductsAdapter.updateItem(product);

                    // Перестроим массив на случай, если изменилась категория
                    Utils.removeCategoriesFromArrayListOfProducts(mProducts);
                    Utils.sortArrayListByCategories(mProducts);
                    if (showCategories()) Utils.addCategoriesInArrayListOfProducts(this, mProducts);
                    mProductsAdapter.notifyDataSetChanged();
                }
        }
    }

    private boolean showCategories(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean(getString(R.string.pref_key_show_categories), false);
    }

    private boolean showImages() {
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPref.getBoolean(getApplicationContext().getString(R.string.pref_key_show_images), true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveCheckedInDB();
    }

    private void saveCheckedInDB(){
        // Сохраним "вычеркивания" в БД
        mShoppingList.setProducts(mProducts);
        mShoppingList.updateCheckedInDB(this);
    }
}

package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapterShoppingListInShop;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import java.util.ArrayList;

public class ShoppingListInShopActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Product> mProducts;
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

        // Получим ссылки на фрагемнты
        FragmentManager fragmentManager = getFragmentManager();
        ShoppingListFragment shoppingListFragment = (ShoppingListFragment)fragmentManager.findFragmentById(R.id.frgShoppingListInShop);

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
        shoppingListFragment.setListAdapter(mProductsAdapter);

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

        mProducts.clear();
        while (data.moveToNext()){
            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    ShoppingListContentProvider.getImageUri(data), data.getFloat(keyCountIndex));
            mProducts.add(newProduct);
        }

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
}

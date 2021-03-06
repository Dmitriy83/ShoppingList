package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterShoppingListInShop;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;

import java.util.ArrayList;

public class ShoppingListInShopActivity extends BaseActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<IListItem> mProducts;
    private ListAdapterShoppingListInShop mProductsAdapter;
    private ShoppingList mShoppingList;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shopping_list_in_shop);

        // Получим значения из переданных параметров родительской активности
        mShoppingList = getIntent().getParcelableExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue() );

        // На всякий случай
        if (mShoppingList == null) mShoppingList = new ShoppingList(-1, "");

        // Установим заголовок активности
        setTitle(mShoppingList.getName());

        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvProducts);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

        // Создаем массив для хранения списка покупок
        if (savedInstanceState == null) {
            mProducts = new ArrayList<>();
        }
        else{
            mProducts = savedInstanceState.getParcelableArrayList(EXTRAS_KEYS.PRODUCTS_ORIGINAL_VALUES.getValue());
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
            mProductsAdapter.setOriginalValues();
            mShoppingList.setFiltered(savedInstanceState.getBoolean(EXTRAS_KEYS.IS_FILTERED.getValue()));
            // Отфильтруем список, если он ранее был отфильтрован
            if (mShoppingList.isFiltered()) {
                // Иконка фильтрации устанавливается в onCreateOptionsMenu
                mProductsAdapter.hideChecked();
            }
        }

        // Откроем подсказку, если необходимо
        if (Utils.showHelpInShop(getApplicationContext()))
            startActivity(new Intent(this, HelpShoppingListInShopActivity.class));

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView)findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        //outState.putParcelableArrayList(EXTRAS_KEYS.PRODUCTS.getValue(), mProducts);
        mProductsAdapter.setOriginalValues(); // На случай, если список до этого не фильтровали
        outState.putParcelableArrayList(EXTRAS_KEYS.PRODUCTS_ORIGINAL_VALUES.getValue(), mProductsAdapter.getOriginalValues());
        outState.putBoolean(EXTRAS_KEYS.IS_FILTERED.getValue(), mProductsAdapter.isFiltered());

        super.onSaveInstanceState(outState);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                SL_ContentProvider.getShoppingListContentProjection(),
                SL_ContentProvider.KEY_SHOPPING_LIST_ID + "=" + mShoppingList.getId(), null ,null);
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

        // Отфильтруем список, если он ранее был отфильтрован
        if (mShoppingList.isFiltered()) {
            // Иконка фильтрации устанавливается в onCreateOptionsMenu
            mProductsAdapter.hideChecked();
        }
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

        switch (id) {
            case R.id.action_filter: {
                if (mProductsAdapter.isFiltered()) {
                    setFilterItemUnselected();
                    mProductsAdapter.showChecked();
                    mShoppingList.setFiltered(false);
                } else {
                    setFilterItemSelected();
                    mProductsAdapter.hideChecked();
                    mShoppingList.setFiltered(true);
                }
                break;
            }
            case R.id.action_edit_shopping_list: {
                saveCheckedInDB();
                mShoppingList.startEditingActivity(this);
                finish();
                break;
            }
            case R.id.action_share: {
                prepareShoppingListForSending();
                mShoppingList.share(this);
                break;
            }
            case R.id.action_load_list: {
                mShoppingList.startLoadShoppingListActivity(this);
                finish();
                break;
            }
            case R.id.action_remove_checked: {
                // Сначала необходимо снять фильтр
                removeFilter();
                // сначала заполним объект списком для изменения
                Utils.removeCategoriesFromArrayListOfProducts(mProducts);
                mShoppingList.setProducts(mProducts);
                // удалим вычеркнутые элементы из БД
                mShoppingList.removeCheckedFromDB(this);
                if (showCategories()) Utils.addCategoriesInArrayListOfProducts(this, mProducts);
                // заменим массив для фильтрации
                ArrayList<IListItem> originalValues = new ArrayList<>(mProducts);
                mProductsAdapter.setOriginalValues(originalValues);
                mProductsAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_deselect_all: {
                mProductsAdapter.deselectAll();
                mProductsAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_send_to_friend: {
                prepareShoppingListForSending();
                mShoppingList.openChooseRecipientActivity(this);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareShoppingListForSending() {
        // Создадим вспомагательный массив и удалим из него категории
        ArrayList<IListItem> array;
        // mProductsAdapter.getOriginalValues() может быть равен null, если фильтр еще не накладывался
        if (mProductsAdapter.getOriginalValues() == null) {
            array = new ArrayList<>(mProducts);
        } else {
            array = new ArrayList<>(mProductsAdapter.getOriginalValues());
        }
        Utils.removeCategoriesFromArrayListOfProducts(array);
        mShoppingList.setProducts(array);
    }

    private void removeFilter() {
        if (mProductsAdapter.isFiltered()) {
            setFilterItemUnselected();
            mProductsAdapter.showChecked();
        }
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
                    Product product = data.getParcelableExtra(EXTRAS_KEYS.PRODUCT.getValue());
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
    protected void onPause() {
        super.onPause();

        // Сохраняем значение отфильтрован или нет здесь, т.к. onStop этой активности вызывается после onCreate новой
        mShoppingList.saveFilteredValueInDB(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveCheckedInDB();
    }

    private void saveCheckedInDB(){
        // Сохраним "вычеркивания" в БД
        ArrayList<IListItem> values = mProductsAdapter.getOriginalValues();
        if (values == null) values = mProducts; // Если ранее фильтр не ставился
        mShoppingList.setProducts(values);
        mShoppingList.updateCheckedInDB(this);
    }
}

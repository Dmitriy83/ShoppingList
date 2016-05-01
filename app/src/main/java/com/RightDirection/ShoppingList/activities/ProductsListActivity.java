package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.RightDirection.ShoppingList.helpers.ListAdapterProductsList;
import com.RightDirection.ShoppingList.views.ItemsListFragment;
import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;

import java.util.ArrayList;

public class ProductsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        IOnDeleteItemListener, IOnClickItemListener {

    private ArrayList<ListItem> productListItems;
    private ListAdapterProductsList productListItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        setTitle(R.string.action_edit_products_list);

        // Добавим обработчики кликов по кнопкам
        FloatingActionButton fabAddProduct = (FloatingActionButton) findViewById(R.id.fabProductListAddProduct);
        fabAddProduct.setOnClickListener(onFabAddProductClick);

        // Получим ссылку на фрагемнт
        FragmentManager fragmentManager = getFragmentManager();
        ItemsListFragment productsListFragment = (ItemsListFragment)fragmentManager.findFragmentById(R.id.frgProductList);

        // Создаем массив для хранения списка товаров
        productListItems = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        productListItemsAdapter = new ListAdapterProductsList(this, R.layout.list_item_products_list, productListItems);

        // Привяжем адаптер к фрагменту
        productsListFragment.setListAdapter(productListItemsAdapter);

        // Обновим список товаров из базы данных
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    private View.OnClickListener onFabAddProductClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ItemActivity.class);
            startActivity(intent);
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this, ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);

        productListItems.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            productListItems.add(newListItem);
        }

        productListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onDeleteItem(ListItem item) {
        onResume();
    }

    @Override
    public void OnClickItem(Cursor cursor) {
        // Откроем окно редактирования элемента списка продуктов
        String productName = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_NAME));
        String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
        Intent intent = new Intent(getBaseContext(), ItemActivity.class);
        intent.putExtra("Name", productName);
        intent.putExtra("itemId", itemId);
        intent.putExtra("isNewItem", false);
        startActivity(intent);
    }
}

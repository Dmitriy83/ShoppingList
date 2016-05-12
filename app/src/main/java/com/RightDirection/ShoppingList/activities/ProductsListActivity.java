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

import java.util.ArrayList;
import java.util.Comparator;

public class ProductsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
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

        // Отсортируем список по алфавиту
        productListItemsAdapter.sort(new Comparator<ListItem>() {
            @Override
            public int compare(ListItem lhs, ListItem rhs) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                return res;
            }
        });
        productListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

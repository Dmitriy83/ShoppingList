package com.RightDirection.ShoppingList.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterProductsList;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.utils.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProductsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<Product> mProducts;
    private ListAdapterProductsList mProductsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        setTitle(R.string.action_edit_products_list);

        // Добавим обработчики кликов по кнопкам
        FloatingActionButton fabAddProduct = (FloatingActionButton) findViewById(R.id.fabProductListAddProduct);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(onFabAddProductClick);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rvProducts);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Создаем массив для хранения списка товаров
        mProducts = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mProductsAdapter = new ListAdapterProductsList(this, R.layout.list_item_products_list, mProducts);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mProductsAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);
    }

    private final View.OnClickListener onFabAddProductClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ProductActivity.class);
            startActivityForResult(intent, Utils.NEED_TO_UPDATE);
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);
        int keyCategoryIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_ID);
        int keyCategoryNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_NAME);
        int keyCategoryOrderIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_CATEGORY_ORDER);

        mProducts.clear();
        while (data.moveToNext()){
            Category category = new Category(data.getLong(keyCategoryIndex), data.getString(keyCategoryNameIndex),
                    data.getInt(keyCategoryOrderIndex));

            Product newProduct = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    ShoppingListContentProvider.getImageUri(data), 0, category);
            mProducts.add(newProduct);
        }

        // Отсортируем список по алфавиту
        Collections.sort(mProducts, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });
        mProductsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
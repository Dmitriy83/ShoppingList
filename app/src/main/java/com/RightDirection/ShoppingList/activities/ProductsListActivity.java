package com.RightDirection.ShoppingList.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterProductsList;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProductsListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<IListItem> mProducts;
    private ListAdapterProductsList mProductsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        setTitle(R.string.action_edit_products_list);

        // Добавим обработчики кликов по кнопкам
        FloatingActionButton fabAddProduct = findViewById(R.id.fabProductListAddProduct);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onFabAddProductClick(view); }
            });
        }

        CustomRecyclerView recyclerView = findViewById(R.id.rvProducts);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));
        //recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Создаем массив для хранения списка товаров
        mProducts = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mProductsAdapter = new ListAdapterProductsList(this, mProducts);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mProductsAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Подключим меню
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);
    }

    private void onFabAddProductClick(View view) {
        Intent intent = new Intent(view.getContext(), ProductActivity.class);
        startActivityForResult(intent, Utils.NEED_TO_UPDATE);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SL_ContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProducts.clear();
        while (data.moveToNext()){
            Unit defaultUnit = new Unit(
                    data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_ID)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_NAME)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_SHORT_NAME)));
            mProducts.add(new Product(data, new Category(data), defaultUnit, null));
        }

        // Отсортируем список по алфавиту
        Collections.sort(mProducts, new Comparator<IListItem>() {
            @Override
            public int compare(IListItem lhs, IListItem rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });
        mProductsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
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
import com.RightDirection.ShoppingList.adapters.ListAdapterCategoriesList;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CategoriesListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<IListItem> mCategories;
    private ListAdapterCategoriesList mCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_list);
        setTitle(R.string.action_edit_categories_list);

        // Добавим обработчики кликов по кнопкам
        FloatingActionButton fabAddProduct = (FloatingActionButton) findViewById(R.id.fabAddCategory);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(onFabAddCategoryClick);
        }

        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvCategories);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

        // Создаем массив для хранения списка товаров
        mCategories = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mCategoriesAdapter = new ListAdapterCategoriesList(this, mCategories);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mCategoriesAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);
    }

    private final View.OnClickListener onFabAddCategoryClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CategoryActivity.class);
            startActivity(intent);
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SL_ContentProvider.CATEGORIES_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCategories.clear();
        while (data.moveToNext()){
            Category newCategory = new Category(data);
            mCategories.add(newCategory);
        }

        // Отсортируем список по алфавиту
        Collections.sort(mCategories, new Comparator<IListItem>() {
            @Override
            public int compare(IListItem lhs, IListItem rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });
        mCategoriesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

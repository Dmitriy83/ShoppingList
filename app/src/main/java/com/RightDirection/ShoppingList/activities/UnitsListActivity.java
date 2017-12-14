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
import com.RightDirection.ShoppingList.adapters.ListAdapterUnitsList;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UnitsListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<IListItem> mUnits;
    private ListAdapterUnitsList mUnitsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units_list);
        setTitle(R.string.action_edit_units_list);

        // Добавим обработчики кликов по кнопкам
        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddUnit);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onFabAddUnitClick(view); }
            });
        }

        CustomRecyclerView recyclerView = findViewById(R.id.rvUnits);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

        // Создаем массив для хранения списка единиц измерения
        mUnits = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mUnitsAdapter = new ListAdapterUnitsList(this, mUnits);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mUnitsAdapter);

        // Обновим список ед. измерений из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);

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

    private void onFabAddUnitClick(View view) {
        Intent intent = new Intent(view.getContext(), UnitActivity.class);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SL_ContentProvider.UNITS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mUnits.clear();
        while (data.moveToNext()){
            Unit newUnit = new Unit(
                    data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_ID)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_NAME)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_SHORT_NAME)));
            mUnits.add(newUnit);
        }

        // Отсортируем список по ключу
        Collections.sort(mUnits, new Comparator<IListItem>() {
            @Override
            public int compare(IListItem lhs, IListItem rhs) { return (lhs.getId() < rhs.getId()) ? -1 : ((lhs.getId() == rhs.getId()) ? 0 : 1); }
        });
        mUnitsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

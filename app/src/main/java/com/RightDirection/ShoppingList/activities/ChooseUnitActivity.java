package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterChooseUnit;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChooseUnitActivity extends BaseActivity
        implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<Unit> mUnits;
    private ListAdapterChooseUnit mUnitsAdapter;
    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_unit);

        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvUnits);
        if (recyclerView == null) return;

        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

        // Создаем массив для хранения списка товаров
        mUnits = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mUnitsAdapter = new ListAdapterChooseUnit(this, mUnits);


        if (savedInstanceState != null) {
            // Восстановим объект из сохраненных значений
            mProduct = savedInstanceState.getParcelable(EXTRAS_KEYS.PRODUCT.getValue());
        } else {
            // Получим значения из переданных параметров
            mProduct = getIntent().getParcelableExtra(EXTRAS_KEYS.PRODUCT.getValue());
        }
        mUnitsAdapter.setProduct(mProduct);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mUnitsAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView)findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
        Collections.sort(mUnits, new Comparator<Unit>() {
            @Override
            public int compare(Unit lhs, Unit rhs) { return (lhs.getId() < rhs.getId()) ? -1 : ((lhs.getId() == rhs.getId()) ? 0 : 1); }
        });
        mUnitsAdapter.notifyItemRangeInserted(0, mUnits.size()-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterChooseCategory;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChooseCategoryActivity extends BaseActivity
        implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<Category> mCategories;
    private ListAdapterChooseCategory mCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);

        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvCategories);
        if (recyclerView == null) return;

        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

        // Создаем массив для хранения списка товаров
        mCategories = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mCategoriesAdapter = new ListAdapterChooseCategory(this, mCategories);

        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mCategoriesAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView)findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
        Collections.sort(mCategories, new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });
        mCategoriesAdapter.notifyItemRangeInserted(0, mCategories.size()-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

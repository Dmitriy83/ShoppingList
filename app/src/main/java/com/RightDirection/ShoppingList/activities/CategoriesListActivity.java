package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.RightDirection.ShoppingList.Category;
import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapterCategoriesList;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import java.util.ArrayList;
import java.util.Comparator;

public class CategoriesListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<Category> mCategories;
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

        // Получим ссылку на фрагемнт
        FragmentManager fragmentManager = getFragmentManager();
        ShoppingListFragment categoriesListFragment = (ShoppingListFragment)fragmentManager.findFragmentById(R.id.frgCategoriesList);

        // Создаем массив для хранения списка товаров
        mCategories = new ArrayList<>();

        // Создадим новый адаптер для работы со списком товаров
        mCategoriesAdapter = new ListAdapterCategoriesList(this, R.layout.list_item_products_list, mCategories);

        // Привяжем адаптер к фрагменту
        categoriesListFragment.setListAdapter(mCategoriesAdapter);

        // Обновим список товаров из базы данных - запускается в onResume
        getLoaderManager().initLoader(0, null, this);
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
            // TODO: Добавление новой категории

        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ShoppingListContentProvider.CATEGORIES_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);

        mCategories.clear();
        while (data.moveToNext()){
            Category newProduct = new Category(data.getLong(keyIdIndex), data.getString(keyNameIndex));
            mCategories.add(newProduct);
        }

        // Отсортируем список по алфавиту
        mCategoriesAdapter.sort(new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });
        mCategoriesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

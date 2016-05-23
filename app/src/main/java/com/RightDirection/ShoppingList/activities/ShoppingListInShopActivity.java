package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapterShoppingListInShop;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ItemsListFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ShoppingListInShopActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<ListItem> mShoppingListItems;
    private ListAdapterShoppingListInShop mShoppingListItemsAdapter;
    private String mListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_shopping_list_in_shop);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

        // Получим значения из переданных параметров родительской активности
        Intent sourceIntent = getIntent();
        mListId = sourceIntent.getStringExtra(String.valueOf(R.string.list_id));

        // Получим ссылки на фрагемнты
        FragmentManager fragmentManager = getFragmentManager();
        ItemsListFragment shoppingListFragment = (ItemsListFragment)fragmentManager.findFragmentById(R.id.frgShoppingListInShop);

        // Создаем массив для хранения списка покупок
        if (savedInstanceState == null) {
            mShoppingListItems = new ArrayList<>();
        }
        else{
            mShoppingListItems = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.shopping_list_items));
        }

        // Создадим новый адаптер для работы со списком покупок
        mShoppingListItemsAdapter = new ListAdapterShoppingListInShop(this, R.layout.list_item_shopping_list_in_shop, mShoppingListItems);

        // Привяжем адаптер к фрагменту-списку
        shoppingListFragment.setListAdapter(mShoppingListItemsAdapter);

        if (savedInstanceState == null) {
            // Заполним список покупок из базы данных
            getLoaderManager().initLoader(0, null, this);
        }
        else{
            mShoppingListItemsAdapter.setIsFiltered(savedInstanceState.getBoolean(String.valueOf(R.string.is_filtered)));
            ArrayList<ListItem> originalValues = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.shopping_list_items_original_values));
            mShoppingListItemsAdapter.setOriginalValues(originalValues);
        }

        Button btnFilter = (Button)findViewById(R.id.btnFilter);
        if (btnFilter != null){
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnFilter.setTransformationMethod(null);
            btnFilter.setOnClickListener(onBtnFilterClick);
            if (savedInstanceState != null || mShoppingListItemsAdapter.isFiltered()) {
                btnFilter.setText(R.string.show_marked);
            }
            else{
                btnFilter.setText(R.string.hide_marked);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        outState.putParcelableArrayList(String.valueOf(R.string.shopping_list_items), mShoppingListItems);
        outState.putParcelableArrayList(String.valueOf(R.string.shopping_list_items_original_values), mShoppingListItemsAdapter.getOriginalValues());
        outState.putBoolean(String.valueOf(R.string.is_filtered), mShoppingListItemsAdapter.isFiltered());

        super.onSaveInstanceState(outState);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListId, null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);

        mShoppingListItems.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            mShoppingListItems.add(newListItem);
        }

        mShoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    private View.OnClickListener onBtnFilterClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btnFilter = (Button)view;
            if (mShoppingListItemsAdapter.isFiltered()) {
                btnFilter.setText(R.string.hide_marked);
                mShoppingListItemsAdapter.showMarked();
            }
            else{
                btnFilter.setText(R.string.show_marked);
                mShoppingListItemsAdapter.hideMarked();
            }

        }
    };

}

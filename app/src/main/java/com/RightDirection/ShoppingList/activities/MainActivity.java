package com.RightDirection.ShoppingList.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapterMainActivity;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        InputListNameDialog.IInputListNameDialogListener{

    private ArrayList<ListItem> mShoppingLists;
    private ListAdapterMainActivity mShoppingListsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Запустим главную активность
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Подключим обработчики
        FloatingActionButton fabAddNewShoppingList = (FloatingActionButton) findViewById(R.id.fabAddNewShoppingList);
        if (fabAddNewShoppingList != null) {
            fabAddNewShoppingList.setOnClickListener(onFabAddNewShoppingListClick);
        }

        // Получим ссылки на фрагемнты
        android.app.FragmentManager fragmentManager = getFragmentManager();
        ShoppingListFragment shoppingListFragment = (ShoppingListFragment)fragmentManager.findFragmentById(R.id.frgShoppingLists);

        // Создаем массив для хранения списков покупок
        mShoppingLists = new ArrayList<>();

        // Создадим новый адаптер для работы со списками покупок
        mShoppingListsAdapter = new ListAdapterMainActivity(this, R.layout.list_item_main_activity,
                mShoppingLists);

        // Привяжем адаптер к фрагменту
        shoppingListFragment.setListAdapter(mShoppingListsAdapter);

        // Заполним списки покупок из базы данных
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Заполним меню (добавим элементы из menu_main.xml).
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработаем нажатие на элемент подменю.
        int id = item.getItemId();

        View view = findViewById(android.R.id.content);
        if (id == R.id.action_settings) {
            if (view != null) {
                Context context = view.getContext();
                if (context != null) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            }
        }
        else if (view != null && id == R.id.action_edit_products_list) {
            Intent intent = new Intent(view.getContext(), ProductsListActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final View.OnClickListener onFabAddNewShoppingListClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_list), true);
            startActivity(intent);
        }
    };

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
       return new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);

        mShoppingLists.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex), null);
            mShoppingLists.add(newListItem);
        }

        mShoppingListsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public void onDialogPositiveClick(String listName, String listID) {

        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ShoppingListContentProvider.KEY_NAME, listName);
        contentResolver.update(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, ShoppingListContentProvider.KEY_ID +  " = " + listID, null);

        mShoppingListsAdapter.updateItem(listID, listName, null);
    }

    @Override
    public void onDialogNegativeClick() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

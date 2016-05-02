package com.RightDirection.ShoppingList.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnEditItemListener;
import com.RightDirection.ShoppingList.views.ItemsListFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        IOnDeleteItemListener, IOnClickItemListener, IOnEditItemListener {

    private ArrayList<ListItem> shoppingLists;
    private ListAdapterMainActivity shoppingListsAdapter;

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
        fabAddNewShoppingList.setOnClickListener(onFabAddNewShoppingListClick);

        // Получим ссылки на фрагемнты
        FragmentManager fragmentManager = getFragmentManager();
        ItemsListFragment shoppingListFragment = (ItemsListFragment)fragmentManager.findFragmentById(R.id.frgShoppingLists);

        // Создаем массив для хранения списков покупок
        shoppingLists = new ArrayList<>();

        // Создадим новый адаптер для работы со списками покупок
        shoppingListsAdapter = new ListAdapterMainActivity(this, R.layout.list_item_main_activity, shoppingLists);

        // Привяжем адаптер к фрагменту
        shoppingListFragment.setListAdapter(shoppingListsAdapter);

        // Заполним списки покупок из базы данных
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            Snackbar.make(view, "In developing...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return true;
        }
        else if (id == R.id.action_edit_products_list) {
            Intent intent = new Intent(view.getContext(), ProductsListActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener onFabAddNewShoppingListClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ShoppingListEditingActivity.class);
            intent.putExtra("isNewList", true);
            startActivity(intent);
        }
    };


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                null, null, null ,null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);

        shoppingLists.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            shoppingLists.add(newListItem);
        }

        shoppingListsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public void onDeleteItem(@Nullable ListItem item) {
        final ListItem listItem = item;

        // Выведем вопрос об удалении списка покупок
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.delete_shopping_list_question));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // Удалим запись из БД по id
                        ContentResolver contentResolver = getContentResolver();
                        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                                ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + listItem.getId(), null);
                        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                                ShoppingListContentProvider.KEY_ID + "=" + listItem.getId(), null);

                        // Обновим списки покупок
                        onResume();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog.show();
    }

    @Override
    public void OnClickItem(Cursor cursor) {
        Intent intent = new Intent(this.getBaseContext(), ShoppingListInShopActivity.class);
        String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
        intent.putExtra("listId", itemId);
        startActivity(intent);
    }

    @Override
    public void OnEditItem(Cursor cursor) {
        Intent intent = new Intent(this.getBaseContext(), ShoppingListEditingActivity.class);
        intent.putExtra("isNewList", false);
        String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
        intent.putExtra("listId", itemId);
        startActivity(intent);
    }
}

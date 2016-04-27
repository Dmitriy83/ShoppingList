package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;
import com.RightDirection.ShoppingList.views.ItemsListFragment;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;

import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity implements IOnClickItemListener,
        IOnNewItemAddedListener, IOnDeleteItemListener,
        InputListNameDialog.IInputListNameDialogListener, android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<ListItem> shoppingListItems;
    private ListAdapter shoppingListItemsAdapter;
    private boolean mIsNewList;
    private String mListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_editing_form);

        // Получим значения из переданных параметров родительской активности
        Intent sourceIntent = getIntent();
        mIsNewList = sourceIntent.getBooleanExtra("isNewList", false);
        mListId = sourceIntent.getStringExtra("listId");

        // Добавим обработчики кликов по кнопкам
        Button btnSave = (Button) findViewById(R.id.btnShoppingListSave);
        btnSave.setOnClickListener(onBtnSaveClick);

        Button btnDeleteAllItems = (Button) findViewById(R.id.btnShoppingListDeleteAllItems);
        btnDeleteAllItems.setOnClickListener(onBtnDeleteAllItemsClick);

        // Получим ссылки на фрагемнты
        FragmentManager fragmentManager = getFragmentManager();
        ItemsListFragment shoppingListFragment = (ItemsListFragment)fragmentManager.findFragmentById(R.id.frgShoppingListItem);

        // Создаем массив для хранения списка покупок
        shoppingListItems = new ArrayList<>();

        // Создадим новый адаптер для работы со списком покупок
        shoppingListItemsAdapter = new ListAdapter(this, R.layout.list_item, shoppingListItems);

        // Привяжем адаптер к фрагменту
        shoppingListFragment.setListAdapter(shoppingListItemsAdapter);

        if (!mIsNewList) {
            // Заполним список покупок из базы данных
            getLoaderManager().initLoader(0, null, this);
        }
    }

    private View.OnClickListener onBtnSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mIsNewList) {
                // Откроем окно для ввода наименования нового списка/
                // Сохранение будет производиться в методе onDialogPositiveClick
                InputListNameDialog inputListNameDialog = new InputListNameDialog();
                FragmentManager fragmentManager = getFragmentManager();
                inputListNameDialog.show(fragmentManager, null);
            }
            else {
                // Обновим текущий список покупок
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();

                // Сначала удалим все записи редактируемого списка покупок из БД
                contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                        ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListId, null);

                // Запишем составлящие списка покупок в базу данных
                for (ListItem item: shoppingListItems) {
                    contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, mListId);
                    contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
                    contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
                }

                finish();
            }
        }
    };

    private View.OnClickListener onBtnDeleteAllItemsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            shoppingListItems.clear();
            shoppingListItemsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void OnNewItemAdded(ListItem newItem) {
        shoppingListItems.add(0, newItem);
        shoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteItem(ListItem item) {
        shoppingListItems.remove(item);
        shoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(String listName) {
        // Сохраним список продуктов в БД
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Заполним значения для сохранения в базе данных
        if (mIsNewList) {
            // Запишем новый списко покупок в таблицу SHOPPING_LISTS
            contentValues.put(ShoppingListContentProvider.KEY_NAME, listName);
            Uri insertedId = contentResolver.insert(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
            long listId = ContentUris.parseId(insertedId);
            contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

            // Запишем составлящие списка покупок в базу данных
            for (ListItem item: shoppingListItems) {
                contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, listId);
                contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
                contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
            }
        }

        finish();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    @Override
    public void OnClickItem(Cursor cursor) {

    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mIsNewList){
            return null;
        }

        CursorLoader cursorLoader = new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListId, null ,null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);

        shoppingListItems.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            shoppingListItems.add(newListItem);
        }

        shoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

}

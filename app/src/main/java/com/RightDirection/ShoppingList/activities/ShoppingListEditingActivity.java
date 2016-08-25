package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.helpers.ListAdapterShoppingListEditing;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.helpers.Utils;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;
import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;
import com.RightDirection.ShoppingList.views.ObservableRelativeLayout;

import java.util.ArrayList;

public class ShoppingListEditingActivity extends AppCompatActivity implements IOnNewItemAddedListener,
        InputListNameDialog.IInputListNameDialogListener, android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<ListItem> mShoppingListItems;
    private ListAdapterShoppingListEditing mShoppingListItemsAdapter;
    private boolean mIsNewList;
    private String mListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_editing);

        // Получим значения из переданных параметров родительской активности
        Intent sourceIntent = getIntent();
        mIsNewList = sourceIntent.getBooleanExtra(String.valueOf(R.string.is_new_list), false);
        mListId = sourceIntent.getStringExtra(String.valueOf(R.string.list_id));

        // Добавим обработчики кликов по кнопкам
        Button btnSave = (Button) findViewById(R.id.btnShoppingListSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(onBtnSaveClick);
        }

        Button btnDeleteAllItems = (Button) findViewById(R.id.btnShoppingListDeleteAllItems);
        if (btnDeleteAllItems != null) {
            btnDeleteAllItems.setOnClickListener(onBtnDeleteAllItemsClick);
        }

        // Получим ссылки на фрагемнты
        FragmentManager fragmentManager = getFragmentManager();
        ShoppingListFragment shoppingListFragment = (ShoppingListFragment)fragmentManager.findFragmentById(R.id.frgShoppingList);
        // Добавим фрагемент в качестве Наблюдателя к родительскому контейнеру (для отслеживания события полной отрисовки дочерних элементов)
        ObservableRelativeLayout shoppingListEditingContainerLayout = (ObservableRelativeLayout)findViewById(R.id.shoppingListEditingContainerLayout);
        if (shoppingListEditingContainerLayout != null && shoppingListFragment != null)
            shoppingListEditingContainerLayout.addObserver(shoppingListFragment);

        if (savedInstanceState == null) {
            // Создаем массив для хранения списка покупок
            mShoppingListItems = new ArrayList<>();
        }
        else {
            mShoppingListItems = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.shopping_list_items));
        }

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showImages = sharedPref.getBoolean(getApplicationContext().getString(R.string.pref_key_show_images), true);
        int listItemLayout = R.layout.list_item_shopping_list_editing;
        if (!showImages) listItemLayout = R.layout.list_item_shopping_list_editing_without_image;
        // Создадим новый адаптер для работы со списком покупок
        mShoppingListItemsAdapter = new ListAdapterShoppingListEditing(this, listItemLayout, mShoppingListItems);

        // Привяжем адаптер к фрагменту
        if (shoppingListFragment != null) shoppingListFragment.setListAdapter(mShoppingListItemsAdapter);

        if (!mIsNewList && savedInstanceState == null) {
            // Заполним список покупок из базы данных
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        outState.putParcelableArrayList(String .valueOf(R.string.shopping_list_items), mShoppingListItems);
    }

    private final View.OnClickListener onBtnSaveClick = new View.OnClickListener() {
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
                for (ListItem item: mShoppingListItems) {
                    contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, mListId);
                    contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
                    contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
                }

                finish();
            }
        }
    };

    private final View.OnClickListener onBtnDeleteAllItemsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mShoppingListItems.clear();
            mShoppingListItemsAdapter.notifyDataSetChanged();

            Log.i("onBtnDeleteAllClick", "onBtnDeleteAllItemsClick called, notifyDataSetChanged called.");
        }
    };

    @Override
    public void OnNewItemAdded(ListItem newItem) {
        mShoppingListItems.add(0, newItem);
        mShoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogPositiveClick(String listName, String id) {
        // Сохраним список продуктов в БД
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Заполним значения для сохранения в базе данных
        if (mIsNewList) {
            // Запишем новый список покупок в таблицу SHOPPING_LISTS
            contentValues.put(ShoppingListContentProvider.KEY_NAME, listName);
            Uri insertedId = contentResolver.insert(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, contentValues);
            long listId = ContentUris.parseId(insertedId);
            contentValues.clear(); // Очистим значения для вставки для дальнейшей записи составляющих списка покупок

            // Запишем составлящие списка покупок в базу данных
            for (ListItem item: mShoppingListItems) {
                contentValues.put(ShoppingListContentProvider.KEY_SHOPPING_LIST_ID, listId);
                contentValues.put(ShoppingListContentProvider.KEY_PRODUCT_ID, item.getId());
                contentResolver.insert(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI, contentValues);
            }
        }

        finish();
    }

    @Override
    public void onDialogNegativeClick() {}

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mIsNewList){
            return null;
        }

        return new CursorLoader(this, ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListId, null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_PRODUCT_ID);
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);

        mShoppingListItems.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex), ShoppingListContentProvider.getImageUri(data));
            mShoppingListItems.add(newListItem);
        }

        mShoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Utils.NEED_TO_UPDATE:
                if (resultCode == RESULT_OK) {
                    // Получим значения из переданных параметров
                    String id = data.getStringExtra(String.valueOf(R.string.item_id));
                    String name = data.getStringExtra(String.valueOf(R.string.name));
                    String strImageUri = data.getStringExtra(String.valueOf(R.string.item_image));
                    Uri imageUri = null;
                    if (strImageUri != null){
                        imageUri = Uri.parse(strImageUri);
                    }
                    mShoppingListItemsAdapter.updateItem(id, name, imageUri);
                }
        }
    }


}

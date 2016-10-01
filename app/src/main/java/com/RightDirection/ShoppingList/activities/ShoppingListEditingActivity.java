package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.ShoppingList;
import com.RightDirection.ShoppingList.helpers.ListAdapterShoppingListEditing;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.helpers.Utils;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;
import com.RightDirection.ShoppingList.views.ObservableRelativeLayout;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import java.util.ArrayList;

public class ShoppingListEditingActivity extends AppCompatActivity implements IOnNewItemAddedListener,
        InputListNameDialog.IInputListNameDialogListener, android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Product> mShoppingListItems;
    private ListAdapterShoppingListEditing mShoppingListItemsAdapter;
    private boolean mIsNewList;
    private long mListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_editing);

        // Получим значения из переданных параметров родительской активности
        Intent sourceIntent = getIntent();
        mIsNewList = sourceIntent.getBooleanExtra(String.valueOf(R.string.is_new_list), false);
        mListId = sourceIntent.getLongExtra(String.valueOf(R.string.list_id), 0);

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

            // Перед сохранением передадим фокус полю ввода наименования продукта, на случай, если в
            // данный момент редактируется количество с помощью клавиватуры (сохранение количества
            // происходит при потере фокуса)
            AutoCompleteTextView textView = (AutoCompleteTextView)findViewById(R.id.newItemEditText);
            if (textView != null)
                textView.requestFocus();

            if (mIsNewList) {
                // Откроем окно для ввода наименования нового списка/
                // Сохранение будет производиться в методе onDialogPositiveClick
                InputListNameDialog inputListNameDialog = new InputListNameDialog();
                FragmentManager fragmentManager = getFragmentManager();
                inputListNameDialog.show(fragmentManager, null);
            }
            else {
                // Обновим текущий список покупок
                ShoppingList shoppingList = new ShoppingList(mListId, "", null, mShoppingListItems);
                shoppingList.updateInDB(getApplicationContext());

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
    public void OnNewItemAdded(Product newItem) {
        // Если элемент уже присутствует в списке, то добавлять не нужно
        if (!mShoppingListItems.contains(newItem)) {
            mShoppingListItems.add(0, newItem);
            mShoppingListItemsAdapter.notifyDataSetChanged();
        }else{
            // Сообщим о том, что элемент уже есть в списке
            Toast.makeText(this, "Item was added before", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogPositiveClick(String listName, long id) {
        // Сохраним список продуктов в БД
        ShoppingList shoppingList = new ShoppingList(mListId, listName, null, mShoppingListItems);
        shoppingList.addToDB(getApplicationContext());

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
        int keyCountIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);

        mShoppingListItems.clear();
        while (data.moveToNext()){
            Product newListItem = new Product(data.getLong(keyIdIndex), data.getString(keyNameIndex),
                    ShoppingListContentProvider.getImageUri(data), data.getFloat(keyCountIndex));
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
                    long id = data.getLongExtra(String.valueOf(R.string.item_id), 0);
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

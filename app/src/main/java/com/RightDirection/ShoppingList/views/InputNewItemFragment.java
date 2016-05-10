package com.RightDirection.ShoppingList.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;

import java.util.ArrayList;

public class InputNewItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private IOnNewItemAddedListener onNewItemAddedListener;

    // Синхронизируемые массивы (по индексу в списке). Должны изменяться одновременно.
    // 1. Хранит объекты ListItem. Необходим для работы с базой данных
    private ArrayList<ListItem> mAllProducts = new ArrayList<>();
    // 2. Хранит имена объектов ListItem. Необходим для работы с AutoCompleteTextView
    private ArrayList<String> mAllProductsNames = new ArrayList<>();

    private ListItem mCurrentItem = null;
    private ArrayAdapter<String> mAdapter;

    private AutoCompleteTextView mTvNewItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_item_edit_text_fragment, container, false);

        // Обновим список товаров из базы данных
        getLoaderManager().initLoader(0, null, this);

        mTvNewItem = (AutoCompleteTextView)view.findViewById(R.id.newItemEditText);
        mTvNewItem.setOnKeyListener(newItemEditTextOnKey);
        mTvNewItem.setOnItemClickListener(onItemClickListener);

        // Получим активность, к которой будет привязан адаптер
        Activity activity = getActivity();
        mAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, mAllProductsNames);

        // Установим количество символов, которые пользователь должен ввести прежде чем выпадающий список будет показан
        mTvNewItem.setThreshold(1);

        mTvNewItem.setAdapter(mAdapter);

        // Добавим обработчик нажатия для кнопки добавляния нового элемента в базу данных
        Button btnAddProductToShoppingList = (Button) view.findViewById(R.id.btnAddProductToShoppingList);
        btnAddProductToShoppingList.setOnClickListener(onBtnAddProductToShoppingListClickListener);

        return view;
    }

    private AutoCompleteTextView.OnKeyListener newItemEditTextOnKey = new AutoCompleteTextView.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                {
                    addItem();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ENTER){
                    createNewItem();
                    addItem();
                    return true;
                }
            }
            return false;
        }
    };

    private void addItem(){
        onNewItemAddedListener.OnNewItemAdded(mCurrentItem);
        mTvNewItem.setText("");
        mCurrentItem = null;
    }


    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = mAdapter.getItem(position);
            mCurrentItem = mAllProducts.get(mAllProductsNames.indexOf(name));
            addItem();
        }
    };

    private Button.OnClickListener onBtnAddProductToShoppingListClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Добавим новый товар в БД
            createNewItem();

            // Оповестим родительскую активность о выборе элемента
            addItem();
        }
    };

    private void createNewItem() {
        // Добавим новый товар в БД
        ContentResolver contentResolver = getActivity().getContentResolver();
        ContentValues contentValues = new ContentValues();

        String newItemName = mTvNewItem.getText().toString();
        contentValues.put(ShoppingListContentProvider.KEY_NAME, newItemName);
        Uri insertedItemUri = contentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        String insertedItemId = insertedItemUri.getPathSegments().get(1);

        // Добавим новый товар в массив всех товаров текущего фрагмента (для построения списка выпадающего меню)
        mCurrentItem = new ListItem(insertedItemId, newItemName);
        mAllProducts.add(mCurrentItem);
        mAllProductsNames.add(newItemName);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        Activity activity = null;
        if (context instanceof Activity){
            activity = (Activity) context;
        }

        if (activity != null) {
            try {
                onNewItemAddedListener = (IOnNewItemAddedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " должна поддерживать итерфейс OnNewItemAddedListener");
            }
        }
    }

    // При отладке на телефоне в метод с переменной Context программа не входит.
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        try {
            onNewItemAddedListener = (IOnNewItemAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " должна поддерживать итерфейс IOnNewItemAddedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Получим активность, к которой будет привязан адаптер
        Activity activity = getActivity();
        CursorLoader cursorLoader = new CursorLoader(activity, ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow("_id");

        mAllProducts.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            addProductInArrays(newListItem);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void addProductInArrays(ListItem newListItem){
        mAllProducts.add(newListItem);
        mAllProductsNames.add(newListItem.getName());
    }
}


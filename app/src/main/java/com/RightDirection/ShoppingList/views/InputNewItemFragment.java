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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;

import java.util.ArrayList;
import java.util.List;

public class InputNewItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private IOnNewItemAddedListener onNewItemAddedListener;

    // Синхронизируемые массивы (по индексу в списке). Должны изменяться одновременно.
    // 1. Хранит объекты ListItem. Необходим для работы с базой данных
    private ArrayList<ListItem> mAllProducts;
    // 2. Хранит имена объектов ListItem. Необходим для работы с AutoCompleteTextView
    private ArrayList<String> mAllProductsNames;

    private ListItem mCurrentItem = null;
    private ArrayAdapter<String> mAdapter;

    private AutoCompleteTextView mTvNewItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_item_edit_text_fragment, container, false);

        if (savedInstanceState == null) {
            mAllProducts = new ArrayList<>();
            mAllProductsNames = new ArrayList<>();
        }
        else{
            mAllProducts = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.all_products));
            mAllProductsNames = savedInstanceState.getStringArrayList(String.valueOf(R.string.all_products_names));
        }

        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, mAllProductsNames);

        mTvNewItem = (AutoCompleteTextView)view.findViewById(R.id.newItemEditText);
        if (mTvNewItem != null) {
            mTvNewItem.setOnEditorActionListener(newItemEditTextOnEditorActionListener);
            mTvNewItem.setOnItemClickListener(onItemClickListener);
        }
        // Добавим обработчик нажатия для кнопки добавляния нового элемента в базу данных
        Button btnAddProductToShoppingList = (Button) view.findViewById(R.id.btnAddProductToShoppingList);
        if (btnAddProductToShoppingList != null) {
            btnAddProductToShoppingList.setOnClickListener(onBtnAddProductToShoppingListClickListener);
        }

        // Установим количество символов, которые пользователь должен ввести прежде чем выпадающий список будет показан
        mTvNewItem.setThreshold(1);

        mTvNewItem.setAdapter(mAdapter);

        // Обновим список товаров из базы данных
        if (savedInstanceState == null) {
            getLoaderManager().initLoader(0, null, this);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Сохраним список продуктов для подбора
        outState.putParcelableArrayList(String.valueOf(R.string.all_products), mAllProducts);
        outState.putStringArrayList(String.valueOf(R.string.all_products_names), mAllProductsNames);
    }

    private final AutoCompleteTextView.OnEditorActionListener newItemEditTextOnEditorActionListener = new AutoCompleteTextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createNewItem();
                addItem();
                return true;
            }
            return false;
        }
    };

    private void addItem(){
        onNewItemAddedListener.OnNewItemAdded(mCurrentItem);
        mTvNewItem.setText("");
        mCurrentItem = null;
    }


    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = mAdapter.getItem(position);
            mCurrentItem = mAllProducts.get(mAllProductsNames.indexOf(name));
            addItem();
        }
    };

    private final Button.OnClickListener onBtnAddProductToShoppingListClickListener = new Button.OnClickListener() {
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
        if (insertedItemUri != null) {
            List<String> pathSegments = insertedItemUri.getPathSegments();
            if (pathSegments != null) {

                long insertedItemId = Long.parseLong(pathSegments.get(1));

                // Добавим новый товар в массив всех товаров текущего фрагмента (для построения списка выпадающего меню)
                mCurrentItem = new ListItem(insertedItemId, newItemName, null);
                addProductInArrays(mCurrentItem);
                mAdapter.add(newItemName);
            }
        }
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
        return new CursorLoader(getActivity(), ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int keyNameIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
        int keyIdIndex = data.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_ID);

        mAllProducts.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getLong(keyIdIndex), data.getString(keyNameIndex), ShoppingListContentProvider.getImageUri(data));
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


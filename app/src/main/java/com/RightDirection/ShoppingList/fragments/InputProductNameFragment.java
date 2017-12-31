package com.RightDirection.ShoppingList.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class InputProductNameFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private IOnNewItemAddedListener onNewItemAddedListener;

    // Синхронизируемые массивы (по индексу в списке). Должны изменяться одновременно.
    // 1. Хранит объекты ListItem. Необходим для работы с базой данных
    private static ArrayList<Product> mAllProducts;
    // 2. Хранит имена объектов ListItem. Необходим для работы с AutoCompleteTextView
    private static ArrayList<String> mAllProductsNames;

    private Product mCurrentItem = null;
    private static ArrayAdapter<String> mAdapter;

    private AutoCompleteTextView mTvNewItem;

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_product_name, container, false);

        if (savedInstanceState == null) {
            mAllProducts = new ArrayList<>();
            mAllProductsNames = new ArrayList<>();
        }
        else{
            mAllProducts = savedInstanceState.getParcelableArrayList(EXTRAS_KEYS.PRODUCTS.getValue());
            mAllProductsNames = savedInstanceState.getStringArrayList(EXTRAS_KEYS.PRODUCTS_NAMES.getValue());
        }

        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, mAllProductsNames);

        mTvNewItem = view.findViewById(R.id.newItemEditText);
        if (mTvNewItem != null) {
            mTvNewItem.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { return newItemEditTextOnEditorActionListener(actionId); }
            });
            mTvNewItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) { onItemClickListener(position);  }
            });
        }
        // Добавим обработчик нажатия для кнопки добавляния нового элемента в базу данных
        Button btnAddProductToShoppingList = view.findViewById(R.id.btnAddProductToShoppingList);
        if (btnAddProductToShoppingList != null) {
            btnAddProductToShoppingList.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) { onBtnAddProductToShoppingListClickListener(); }
            });
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
        outState.putParcelableArrayList(EXTRAS_KEYS.PRODUCTS.getValue(), mAllProducts);
        outState.putStringArrayList(EXTRAS_KEYS.PRODUCTS_NAMES.getValue(), mAllProductsNames);
    }

    private boolean newItemEditTextOnEditorActionListener(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onBtnAddProductToShoppingListClickListener();
            return true;
        }
        return false;
    }

    private void addItem(){
        onNewItemAddedListener.OnNewItemAdded(mCurrentItem);
        mTvNewItem.setText("");
        mCurrentItem = null;
    }

    private void onItemClickListener(int position) {
        String name = mAdapter.getItem(position);
        int index = mAllProductsNames.indexOf(name);
        if (index >= 0) {
            mCurrentItem = mAllProducts.get(index);
            addItem();
        }
    }

    private void onBtnAddProductToShoppingListClickListener() {
        // Добавим новый товар в БД
        createNewItem();

        // Оповестим родительскую активность о выборе элемента
        addItem();
    }

    private void createNewItem() {
        // Добавим новый товар в БД
        String newItemName = mTvNewItem.getText().toString();
        if (newItemName.isEmpty()){
            Toast.makeText(mContext, mContext.getString(R.string.addItemHint), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mAllProductsNames.contains(newItemName)) {
            mCurrentItem = new Product(Utils.EMPTY_ID, newItemName); // id будет назначено при сохранении продукта в БД
            mCurrentItem.addToDB(getActivity());

            // Добавим новый товар в массив всех товаров текущего фрагмента (для построения списка выпадающего меню)
            addProductInArrays(mCurrentItem);
            mAdapter.add(newItemName);
        }else{
            int indexFoundItem = mAllProductsNames.indexOf(newItemName);
            mCurrentItem = mAllProducts.get(indexFoundItem);
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        mContext = context;

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
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mContext = activity;

        try {
            onNewItemAddedListener = (IOnNewItemAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " должна поддерживать итерфейс IOnNewItemAddedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), SL_ContentProvider.PRODUCTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAllProducts.clear();
        while (data.moveToNext()){
            Unit defaultUnit = new Unit(
                    data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_ID)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_NAME)),
                    data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_UNIT_SHORT_NAME)));
            Product product = new Product(data, new Category(data), defaultUnit, null);
            addProductInArrays(product);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void addProductInArrays(Product newListItem){
        mAllProducts.add(newListItem);
        mAllProductsNames.add(newListItem.getName());
    }

    public void updateProductName(Product product){
        AsyncTaskUpdateProductName asyncTaskUpdateProductName = new AsyncTaskUpdateProductName(this);
        asyncTaskUpdateProductName.execute(product);
    }

    private static class AsyncTaskUpdateProductName extends AsyncTask<Product, Integer, Boolean> {
        private final WeakReference<InputProductNameFragment> fragmentReference;

        // В конструкторе получим слабую ссылку на фрагмент (чтобы избежать утечек памяти - см.
        // https://stackoverflow.com/questions/44309241/warning-this-asynctask-class-should-be-static-or-leaks-might-occur)
        AsyncTaskUpdateProductName(InputProductNameFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Boolean doInBackground(Product... params) {
            Boolean success = false;
            Product product = params[0];

            for (int i = 0; i < mAllProducts.size(); i++) {
                Product currentProduct = mAllProducts.get(i);
                if (currentProduct.getId() == product.getId()){
                    currentProduct.setName(product.getName());
                    // Изменим элемент и в связанном массиве
                    mAllProductsNames.set(i, product.getName());

                    success = true;
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Получим ссылку на фрагмент, если она все еще существует
            InputProductNameFragment fragment = fragmentReference.get();
            if (fragment == null) return;

            // Обновим адаптер
            mAdapter = new ArrayAdapter<>(fragment.getActivity(), R.layout.dropdown_item, mAllProductsNames);
            fragment.mTvNewItem.setAdapter(mAdapter);
        }
    }
}


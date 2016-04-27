package com.RightDirection.ShoppingList.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;

import java.util.ArrayList;

public class InputNewItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private IOnNewItemAddedListener onNewItemAddedListener;

    // Синхронизируемые массивы (по индексу в списке). Должны изменяться одновременно.
    // 1. Хранит объекты ListItem. Необходим для работы с базой данных
    private ArrayList<ListItem> allProducts = new ArrayList<>();
    // 2. Хранит имена объектов ListItem. Необходим для работы с AutoCompleteTextView
    private ArrayList<String> allProductsNames = new ArrayList<>();

    private ListItem currentItem = null;
    private ArrayAdapter<String> adapter;

    private AutoCompleteTextView tvNewItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_item_edit_text_fragment, container, false);

        // Обновим список товаров из базы данных
        getLoaderManager().initLoader(0, null, this);

        tvNewItem = (AutoCompleteTextView)view.findViewById(R.id.newItemEditText);
        tvNewItem.setOnKeyListener(newItemEditTextOnKey);
        tvNewItem.setOnItemClickListener(onItemClickListener);

        // Получим активность, к которой будет привязан адаптер
        Activity activity = getActivity();
        adapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, allProductsNames);

        // Установим количество символов, которые пользователь должен ввести прежде чем выпадающий список будет показан
        tvNewItem.setThreshold(1);

        tvNewItem.setAdapter(adapter);

        return view;
    }

    private AutoCompleteTextView.OnKeyListener newItemEditTextOnKey = new AutoCompleteTextView.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    if (currentItem != null) {
                        addItem();
                        return true;
                    }
                }
            }
            return false;
        }
    };

    private void addItem(){
        onNewItemAddedListener.OnNewItemAdded(currentItem);
        tvNewItem.setText("");
        currentItem = null;
    }


    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = adapter.getItem(position);
            currentItem = allProducts.get(allProductsNames.indexOf(name));
            addItem();
        }
    };

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

        allProducts.clear();
        while (data.moveToNext()){
            ListItem newListItem = new ListItem(data.getString(keyIdIndex), data.getString(keyNameIndex));
            addProductInArrays(newListItem);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void addProductInArrays(ListItem newListItem){
        allProducts.add(newListItem);
        allProductsNames.add(newListItem.getName());
    }
}


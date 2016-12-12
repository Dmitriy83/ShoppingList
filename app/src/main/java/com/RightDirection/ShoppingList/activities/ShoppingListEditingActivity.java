package com.RightDirection.ShoppingList.activities;

import android.app.FragmentManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterShoppingListEditing;
import com.RightDirection.ShoppingList.interfaces.IOnNewItemAddedListener;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.items.ShoppingList;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.contentProvider;

import java.util.ArrayList;

public class ShoppingListEditingActivity extends AppCompatActivity implements IOnNewItemAddedListener,
        InputNameDialog.IInputListNameDialogListener, android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Product> mProducts;
    private ListAdapterShoppingListEditing mShoppingListItemsAdapter;
    private ShoppingList mShoppingList;
    private boolean mGoToInShop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_editing);

        // Получим значения из переданных параметров родительской активности
        mShoppingList = getIntent().getParcelableExtra(String.valueOf(R.string.shopping_list));

        if (mShoppingList == null){
            mShoppingList = new ShoppingList(-1, "");
            mShoppingList.isNew = true;
        }

        if (mShoppingList.isNew){
            // Попробуем получить продукты (если активность открылась из активности загрузки списка товаров)
            mProducts = getIntent().getParcelableArrayListExtra(String.valueOf(R.string.products));
        }

        // Установим заголовок активности
        if (mShoppingList.isNew){
            setTitle(getString(R.string.new_list));
        }else{
            setTitle(mShoppingList.getName());
        }

        if (savedInstanceState != null)
            mProducts = savedInstanceState.getParcelableArrayList(String.valueOf(R.string.products));

        if (mProducts == null) mProducts = new ArrayList<>();

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showImages = sharedPref.getBoolean(getApplicationContext().getString(R.string.pref_key_show_images), true);
        int listItemLayout = R.layout.list_item_shopping_list_editing;
        if (!showImages) listItemLayout = R.layout.list_item_shopping_list_editing_without_image;
        // Создадим новый адаптер для работы со списком покупок
        mShoppingListItemsAdapter = new ListAdapterShoppingListEditing(this, listItemLayout, mProducts);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rvProducts);
        if (recyclerView == null) return;
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Привяжем адаптер к фрагменту
        recyclerView.setAdapter(mShoppingListItemsAdapter);

        if (!mShoppingList.isNew && savedInstanceState == null) {
            // Заполним список покупок из базы данных
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Сохраним редактируемый список (восстановим его потом, например, при смене ориентации экрана)
        outState.putParcelableArrayList(String .valueOf(R.string.products), mProducts);
    }

    private void saveListAndFinish(){
        // Перед сохранением передадим фокус полю ввода наименования продукта, на случай, если в
        // данный момент редактируется количество с помощью клавиватуры (сохранение количества
        // происходит при потере фокуса)
        AutoCompleteTextView textView = (AutoCompleteTextView)findViewById(R.id.newItemEditText);
        if (textView != null)
            textView.requestFocus();

        if (mShoppingList.isNew) {
            // Откроем окно для ввода наименования нового списка/
            // Сохранение будет производиться в методе onDialogPositiveClick
            InputNameDialog inputNameDialog = new InputNameDialog();
            FragmentManager fragmentManager = getFragmentManager();
            inputNameDialog.show(fragmentManager, null);
        }
        else {
            // Обновим текущий список покупок
            mShoppingList.setProducts(mProducts);
            mShoppingList.updateInDB(getApplicationContext());

            if (mGoToInShop) {
                // Перейдем к активности "В магазине"
                Intent intent = new Intent(this, ShoppingListInShopActivity.class);
                intent.putExtra(String.valueOf(R.string.shopping_list), mShoppingList);
                startActivity(intent);
            }

            finish();
        }
    }

    private void removeAllItems(){
        mProducts.clear();
        mShoppingListItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnNewItemAdded(Product newItem) {
        // Если элемент уже присутствует в списке, то добавлять не нужно
        if (!mProducts.contains(newItem)) {
            mProducts.add(0, newItem);
            mShoppingListItemsAdapter.notifyDataSetChanged();
        }else{
            // Сообщим о том, что элемент уже есть в списке
            Toast.makeText(this, getString(R.string.item_already_added), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogPositiveClick(String name, long productId, boolean isProduct) {

        if (isProduct) {
            // Создадим вспомогательный объект Product и вызовем команду переименования
            Product renamedProduct = new Product(productId, name, null);
            renamedProduct.renameInDB(getApplicationContext());
            mShoppingListItemsAdapter.updateItem(renamedProduct);
        }else {
            // Сохраним список продуктов в БД
            mShoppingList.setName(name);
            mShoppingList.setProducts(mProducts);
            mShoppingList.addToDB(getApplicationContext());

            if (mGoToInShop) {
                // Перейдем к активности "В магазине"
                Intent intent = new Intent(this, ShoppingListInShopActivity.class);
                intent.putExtra(String.valueOf(R.string.shopping_list), mShoppingList);
                startActivity(intent);
            }

            finish();
        }
    }

    @Override
    public void onDialogNegativeClick() {}

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mShoppingList.isNew){
            return null;
        }

        return new CursorLoader(this, contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, contentProvider.KEY_SHOPPING_LIST_ID + "=" + mShoppingList.getId(), null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mProducts.clear();
        while (data.moveToNext()){
            mProducts.add(new Product(data, new Category(data)));
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
                    Product product = data.getParcelableExtra(String.valueOf(R.string.product));
                    mShoppingListItemsAdapter.updateItem(product);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_shopping_list_editing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработаем нажатие на элемент подменю.
        int id = item.getItemId();

        View view = findViewById(android.R.id.content);
        if (view == null) return super.onOptionsItemSelected(item);

        if (id == R.id.action_save_list) {
            saveListAndFinish();
        }
        else if (id == R.id.action_remove_all_items) {
            removeAllItems();
        }
        else if (id == R.id.action_go_to_in_shop_activity) {
            // Cохраним список покупок и перейдем к активности "В магазине"
            mGoToInShop = true;
            saveListAndFinish();
        }
        else if (id == R.id.action_send_by_email) {
            if (mShoppingList.getName() == null) mShoppingList.setName(getString(R.string.no_name));
            mShoppingList.setProducts(mProducts);
            mShoppingList.sendByEmail(this);
        }
        else if (id == R.id.action_load_list) {
            Intent intentLoad = new Intent(this, LoadShoppingListActivity.class);
            intentLoad.putExtra(String.valueOf(R.string.shopping_list), mShoppingList);
            startActivity(intentLoad);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

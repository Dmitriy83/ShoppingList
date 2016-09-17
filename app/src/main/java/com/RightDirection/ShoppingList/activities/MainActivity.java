package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.RightDirection.ShoppingList.EmailReceiver;
import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.WrongEmailProtocolException;
import com.RightDirection.ShoppingList.helpers.DBUtils;
import com.RightDirection.ShoppingList.helpers.ListAdapterMainActivity;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.helpers.Utils;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import java.util.ArrayList;
import java.util.Calendar;

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
        if (view == null) return super.onOptionsItemSelected(item);

        if (id == R.id.action_settings) {
            Context context = view.getContext();
                if (context != null) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
        }
        else if (id == R.id.action_edit_products_list) {
            Intent intent = new Intent(view.getContext(), ProductsListActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_receive_shopping_list_by_email) {
            try {
                // Получим параметры подключения из настроек приложения
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String host = sharedPref.getString(getApplicationContext().getString(R.string.pref_key_email_host), "");
                String port = sharedPref.getString(getApplicationContext().getString(R.string.pref_key_email_port), "");
                String userName = sharedPref.getString(getApplicationContext().getString(R.string.pref_key_email_login), "");
                String password = sharedPref.getString(getApplicationContext().getString(R.string.pref_key_email_password), "");

                // В отдельном потоке скачем и обработаем электронные письма
                asyncTaskDownloadEmail asyncTaskDownloadEmail = new asyncTaskDownloadEmail();
                EmailReceiver receiver = new EmailReceiver(this);
                receiver.setServerProperties(host, port);
                receiver.setLogin(userName);
                receiver.setPassword(password);
                asyncTaskDownloadEmail.execute(receiver);
            } catch (WrongEmailProtocolException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.wrong_email_protocol_exception_message),
                        Toast.LENGTH_LONG).show();
            }
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
        DBUtils.renameShoppingList(this, listID, listName);
        mShoppingListsAdapter.updateItem(listID, listName, null);
    }

    @Override
    public void onDialogNegativeClick() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class asyncTaskDownloadEmail extends AsyncTask<EmailReceiver, Integer, Boolean>{
        @Override
        protected Boolean doInBackground(EmailReceiver... params) {

            if (params.length <= 0) return false;

            try {
                EmailReceiver receiver = params[0];
                ArrayList<String> fileNames = receiver.getShoppingListsJSONFilesFromUnreadEmails();

                // Загружаем новый списков покупок
                for (String fileName: fileNames) {
                    String jsonStr = Utils.getStringFromFile(fileName);

                    ArrayList<ListItem> listItems = Utils.getListItemsArrayFromJSON(jsonStr);

                    // Сначала нужно добавить новые продукты из списка в базу данных.
                    // Синхронизацияя должна производиться по полю Name
                    DBUtils.addNotExistingProductsToDB(getApplicationContext(), listItems);

                    // Установим для элементов списка правильные идентификаторы из базы данных
                    // (поиск по реквизиту Name)
                    listItems = DBUtils.setIdFromDB(getApplicationContext(), listItems);

                    Calendar calendar = Calendar.getInstance();
                    String newListName = Utils.getListNameFromJSON(jsonStr)
                            + calendar.getTime().toString();
                    DBUtils.saveNewShoppingList(getApplicationContext(), newListName, listItems);
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // В случае, успешной загрузки оповестим адаптер об изменении
            if (success) mShoppingListsAdapter.notifyDataSetChanged();
        }
    }
}

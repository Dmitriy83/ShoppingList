package com.RightDirection.ShoppingList.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterMainActivity;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.items.ShoppingList;
import com.RightDirection.ShoppingList.utils.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.EmailReceiver;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.WrongEmailProtocolException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        InputNameDialog.IInputListNameDialogListener{

    private ArrayList<IListItem> mShoppingLists;
    private ListAdapterMainActivity mShoppingListsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Запустим главную активность
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Установим заголовок активности
        setTitle(getString(R.string.main_activity_title));

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Подключим обработчики
        FloatingActionButton fabAddNewShoppingList = (FloatingActionButton) findViewById(R.id.fabAddNewShoppingList);
        if (fabAddNewShoppingList != null) {
            fabAddNewShoppingList.setOnClickListener(onFabAddNewShoppingListClick);
        }

        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvShoppingLists);
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Создаем массив для хранения списков покупок
        mShoppingLists = new ArrayList<>();
        // Создадим новый адаптер для работы со списками покупок
        mShoppingListsAdapter = new ListAdapterMainActivity(this,
                mShoppingLists);

        // Привяжем адаптер к элементу управления
        recyclerView.setAdapter(mShoppingListsAdapter);

        // Заполним списки покупок из базы данных
        getLoaderManager().initLoader(0, null, this);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView)findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);

        // Откроем подсказку, если необходимо
        if (Utils.showHelpMainActivity(getApplicationContext()))
            startActivity(new Intent(this, HelpMainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Заполним меню (добавим элементы из menu_main.xml).
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
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
                AsyncTaskDownloadEmail asyncTaskDownloadEmail = new AsyncTaskDownloadEmail();
                EmailReceiver receiver = new EmailReceiver(this);
                receiver.setServerProperties(host, port);
                receiver.setLogin(userName);
                receiver.setPassword(password);
                asyncTaskDownloadEmail.execute(receiver);

                // Сообщим пользователю о начале загрузки
                Toast.makeText(this, getString(R.string.loading), Toast.LENGTH_LONG).show();
            } catch (WrongEmailProtocolException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.wrong_email_protocol_exception_message),
                        Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.action_edit_categories_list) {
            Intent intent = new Intent(view.getContext(), CategoriesListActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_feedback) {
            try{
                startActivity(Utils.getSendEmailIntent(getString(R.string.feedback_email),
                        getString(R.string.feedback),
                        "\n" + "\n" + "\n" + "\n" + getString(R.string.email_body_divider)
                                + "\n" + Utils.getDeviceName() + "\nAndroid " + Build.VERSION.RELEASE,
                        null));
            }
            catch(ActivityNotFoundException e){
                System.out.println("Exception raises during sending mail. Discription: " + e);
                Toast.makeText(this, R.string.email_activity_not_found_exception_text,
                        Toast.LENGTH_SHORT).show();
            }
            catch(Exception e){
                System.out.println("Exception raises during sending mail. Discription: " + e);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private final View.OnClickListener onFabAddNewShoppingListClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ShoppingListEditingActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
       return new CursorLoader(this, SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                null, null, null ,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Получаем только имена и идентификаторы списков
        mShoppingLists.clear();
        while (data.moveToNext()){
            ShoppingList newShoppingList = new ShoppingList(data);
            mShoppingLists.add(newShoppingList);
        }

        mShoppingListsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public void onDialogPositiveClick(String listName, long listID, boolean isProduct) {
        // Создадим вспомогательный объект ShoppingList и вызовем команду переименования
        ShoppingList renamedSL = new ShoppingList(listID, listName);
        renamedSL.renameInDB(getApplicationContext());
        mShoppingListsAdapter.updateItem(renamedSL);
    }

    @Override
    public void onDialogNegativeClick() {}

    private class AsyncTaskDownloadEmail extends AsyncTask<EmailReceiver, Integer, ArrayList<ShoppingList>>{
        @Override
        protected ArrayList<ShoppingList> doInBackground(EmailReceiver... params) {

            ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();

            if (params.length <= 0) return loadedShoppingLists;

            try {
                EmailReceiver receiver = params[0];
                ArrayList<String> fileNames = receiver.getShoppingListsJSONFilesFromEmails();

                // Загружаем новый списков покупок
                for (String fileName: fileNames) {
                    String jsonStr = Utils.getStringFromFile(fileName);

                    ArrayList<IListItem> products = Utils.getProductsFromJSON(jsonStr);

                    // Сформируем имя нового списка покупок
                    Calendar calendar = Calendar.getInstance();
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    String newListName = Utils.getListNameFromJSON(jsonStr) + " "
                            + getString(R.string.loaded) + " "
                            + dateFormat.format(calendar.getTime());

                    // Создадим  новый объект-лист покупок
                    ShoppingList newShoppingList = new ShoppingList(-1, newListName, products);

                    // Сначала нужно добавить новые продукты из списка в базу данных.
                    // Синхронизацияя должна производиться по полю Name
                    newShoppingList.addNotExistingProductsToDB(getApplicationContext());

                    // Сохраним новый лист покупок в базе данных
                    newShoppingList.addToDB(getApplicationContext());
                    loadedShoppingLists.add(newShoppingList);
                }

                return loadedShoppingLists;
            } catch (Exception e) {
                e.printStackTrace();
                return loadedShoppingLists;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ShoppingList> loadedShoppingLists) {
            super.onPostExecute(loadedShoppingLists);

            // В случае, успешной загрузки оповестим адаптер об изменении
            if (loadedShoppingLists.size() > 0) {
                for (ShoppingList newShoppingList: loadedShoppingLists) {
                    mShoppingListsAdapter.add(newShoppingList);
                }
                mShoppingListsAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.no_emails_for_loading),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

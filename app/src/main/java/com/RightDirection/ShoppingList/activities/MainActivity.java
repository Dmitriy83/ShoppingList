package com.RightDirection.ShoppingList.activities;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.services.ReceiveShoppingListsService;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.TimeoutControl;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.EmailReceiver;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.WrongEmailProtocolException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        InputNameDialog.IInputListNameDialogListener, NavigationView.OnNavigationItemSelectedListener{

    private ArrayList<IListItem> mShoppingLists;
    private ListAdapterMainActivity mShoppingListsAdapter;
    private DrawerLayout mDrawerLayout;
    private BroadcastReceiver mServiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Запустим главную активность
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Установим заголовок активности
        setTitle(getString(R.string.main_activity_title));

        // Подключим панель навигации
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
    protected void onStart() {
        super.onStart();
        displayUserInformation();

        // Запустим сервис получения списков из Firebase
        Intent intent = new Intent(this, ReceiveShoppingListsService.class);
        startService(intent);
    }

    private void displayUserInformation() {
        // Скроем/отобразим кнопки "Sign in"/"Profile"
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu navMenu = navView.getMenu();
        boolean userSignedIn = userSignedIn();
        navMenu.findItem(R.id.action_sign_in).setVisible(!userSignedIn);
        MenuItem actionProfile = navMenu.findItem(R.id.action_profile);
        actionProfile.setVisible(userSignedIn);
        navMenu.findItem(R.id.action_friends).setVisible(userSignedIn);
        navMenu.findItem(R.id.action_receive_shopping_lists).setVisible(userSignedIn);

        if (userSignedIn){
            User user = FirebaseUtil.readUserFromPref(this);
            assert user !=null;
            actionProfile.setTitle(user.getName());
        }
    }

    private boolean userSignedIn() {
        User user = FirebaseUtil.readUserFromPref(this);
        return (user != null);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Необходимо перезапустить загрузчик, например, при смене ориентации экрана
        getLoaderManager().restartLoader(0, null, this);

        // Откроем подсказку, если необходимо
        if (Utils.showHelpMainActivity(getApplicationContext()))
            startActivity(new Intent(this, HelpMainActivity.class));

        final IntentFilter serviceActiveFilter = new IntentFilter(Utils.ACTION_UPDATE_MAIN_ACTIVITY);
        mServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null) {
                    ArrayList<ShoppingList> loadedShoppingLists = intent.getParcelableArrayListExtra(EXTRAS_KEYS.SHOPPING_LISTS.getValue());
                    if (loadedShoppingLists != null){
                        updateWithLoadedShoppingLists(loadedShoppingLists);
                    }
                }
            }
        };
        this.registerReceiver(mServiceReceiver, serviceActiveFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mServiceReceiver);
    }

    private void updateWithLoadedShoppingLists(ArrayList<ShoppingList> loadedShoppingLists) {
        for (ShoppingList newShoppingList: loadedShoppingLists) {
            mShoppingListsAdapter.add(newShoppingList);
        }
        mShoppingListsAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Обработаем нажатие на пункт меню после закрытия панели навигации, чтобы позволить
        // анимации закрытия панели навигации доиграть до конца. Иначе будет заметный лаг.
        handleItemClick(item.getItemId());

        return true;
    }

    private void handleItemClick(int id) {
        switch (id) {
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_edit_products_list: {
                Intent intent = new Intent(this, ProductsListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_receive_shopping_list_by_email: {
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
                break;
            }
            case R.id.action_edit_categories_list: {
                Intent intent = new Intent(this, CategoriesListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_feedback: {
                try {
                    startActivity(Utils.getSendEmailIntent(getString(R.string.feedback_email),
                            getString(R.string.feedback),
                            "\n" + "\n" + "\n" + "\n" + getString(R.string.email_body_divider)
                                    + "\n" + Utils.getDeviceName() + "\nAndroid " + Build.VERSION.RELEASE,
                            null));
                } catch (ActivityNotFoundException e) {
                    System.out.println("Exception raises during sending mail. Discription: " + e);
                    Toast.makeText(this, R.string.email_activity_not_found_exception_text,
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    System.out.println("Exception raises during sending mail. Discription: " + e);
                }
                break;
            }
            case R.id.action_estimate: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.RightDirection.ShoppingList"));
                startActivity(intent);
                break;
            }
            case R.id.action_sign_in:
            case R.id.action_profile:{
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_friends:{
                Intent intent = new Intent(this, FriendsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_receive_shopping_lists:{
                receiveShoppingListsFromFirebase();
                break;
            }
        }
    }

    private void receiveShoppingListsFromFirebase(){
        Toast.makeText(this, R.string.receiving, Toast.LENGTH_SHORT).show();

        final ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();

        final TimeoutControl timeoutControl = new TimeoutControl();
        timeoutControl.addListener(new TimeoutControl.IOnTimeoutListener() {
            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.connection_timeout_exceeded, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        timeoutControl.start();

        final DatabaseReference currentUserRef = FirebaseUtil.getCurrentUserRef();
        if (currentUserRef == null) return;
        currentUserRef.child(FirebaseUtil.SHOPPING_LISTS_PATH)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        timeoutControl.stop();
                        ArrayList<FirebaseShoppingList> firebaseLists = new ArrayList<>();
                        if (!dataSnapshot.hasChildren()) {
                            Toast.makeText(getApplicationContext(), R.string.no_shoppping_for_loading, Toast.LENGTH_LONG).show();
                        } else {
                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                FirebaseShoppingList firebaseShoppingList = childDataSnapshot.getValue(FirebaseShoppingList.class);
                                firebaseShoppingList.setName(childDataSnapshot.getKey());
                                firebaseLists.add(firebaseShoppingList);
                            }
                        }

                        // Загружаем новый списков покупок
                        for (FirebaseShoppingList firebaseList: firebaseLists) {
                            // Сформируем имя нового списка покупок
                            Calendar calendar = Calendar.getInstance();
                            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                            String newListName = firebaseList.getName() + " "
                                    + getString(R.string.loaded) + " "
                                    + dateFormat.format(calendar.getTime());

                            // Создадим  новый объект-лист покупок
                            ShoppingList newShoppingList = new ShoppingList(-1, newListName);
                            newShoppingList.loadProductsFromString(getApplicationContext(), firebaseList.getContent());
                            newShoppingList.addNotExistingProductsToDB(getApplicationContext());
                            // Сначала нужно добавить новые продукты из списка в базу данных.
                            // Синхронизацияя должна производиться по полю Name
                            newShoppingList.addNotExistingProductsToDB(getApplicationContext());
                            // Сохраним новый лист покупок в базе данных
                            newShoppingList.addToDB(getApplicationContext());
                            loadedShoppingLists.add(newShoppingList);
                        }

                        // В случае, успешной загрузки оповестим адаптер об изменении
                        if (loadedShoppingLists.size() > 0) {
                            updateWithLoadedShoppingLists(loadedShoppingLists);

                            // Все загруженные листы следует удалить
                            currentUserRef.child(FirebaseUtil.SHOPPING_LISTS_PATH).removeValue();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.no_shoppping_for_loading),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        timeoutControl.stop();
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                    }
                });
    }

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
                updateWithLoadedShoppingLists(loadedShoppingLists);
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.no_emails_for_loading),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

package com.RightDirection.ShoppingList.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterMainActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.fragments.InputNameDialogFragment;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.services.AlarmReceiver;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.utils.EmailReceiver;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.utils.WrongEmailProtocolException;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends BaseActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        InputNameDialogFragment.IInputListNameDialogListener, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {

    private ArrayList<IListItem> mShoppingLists;
    private ListAdapterMainActivity mShoppingListsAdapter;
    private DrawerLayout mDrawerLayout;
    private BroadcastReceiver mServiceReceiver;
    private static final long CHECK_INTERVAL = 30000;
    private boolean mUserSignInInfoExpanded = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_INVITE = 5;

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
                R.string.navigation_drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                // При след. открытии инфо пользователя должно быть свернуто
                mUserSignInInfoExpanded = false;
                configNavView();
            }
        };
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Подключим обработчики
        FloatingActionButton fabAddNewShoppingList = (FloatingActionButton) findViewById(R.id.fabAddNewShoppingList);
        if (fabAddNewShoppingList != null) {
            fabAddNewShoppingList.setOnClickListener(onFabAddNewShoppingListClick);
        }

        CustomRecyclerView recyclerView = (CustomRecyclerView) findViewById(R.id.rvShoppingLists);
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));

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
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        if (emptyView != null) recyclerView.setEmptyView(emptyView);

        if (FirebaseUtil.userSignedIn(this)) scheduleStartServiceReceiveShoppingListsAlarm();
    }

    // Запустим расписание
    private void scheduleStartServiceReceiveShoppingListsAlarm() {
        // Создаем намерение, которое будет выполняться AlarmReceiver-ом
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Создаем "ожидающее намерение", которое будет срабатывать на событии AlarmManager-а
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Запускаем AlarmManager с текущего момента
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                CHECK_INTERVAL, pIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        configNavView();
    }

    private void configNavView() {
        // Скроем/отобразим кнопки "Sign in"/"Profile"
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        assert navView != null;
        View headerLayout = navView.getHeaderView(0);
        View appTitle = headerLayout.findViewById(R.id.appTitle);
        View userSignInInfo = headerLayout.findViewById(R.id.userSignInInfo);
        navView.getMenu().clear();
        if (FirebaseUtil.userSignedIn(this)) {
            navView.inflateMenu(R.menu.activity_main_menu_authorized);

            appTitle.setVisibility(View.GONE);
            userSignInInfo.setVisibility(View.VISIBLE);

            MenuItem actionSignIn = navView.getMenu().findItem(R.id.action_sign_in);
            if (actionSignIn != null) actionSignIn.setVisible(false);

            User user = FirebaseUtil.readUserFromPref(this);
            if (user != null) {
                TextView txtUserName = (TextView) userSignInInfo.findViewById(R.id.txtUserName);
                txtUserName.setText(user.getName());
                ImageView userPhoto = (ImageView) userSignInInfo.findViewById(R.id.imgUserPhoto);
                Picasso.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .fit()
                        .into(userPhoto);
            }

        } else{
            navView.inflateMenu(R.menu.activity_main_menu);

            appTitle.setVisibility(View.VISIBLE);
            userSignInInfo.setVisibility(View.GONE);
        }

        final View arrow = userSignInInfo.findViewById(R.id.imgArrow);
        arrow.setBackgroundResource(R.drawable.ic_drop_down_arrow); // по умолчанию
        userSignInInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Изменяем стрелку и подменяем меню
                NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
                if (mUserSignInInfoExpanded){
                    navView.getMenu().clear();
                    navView.inflateMenu(R.menu.activity_main_menu_authorized);
                    arrow.setBackgroundResource(R.drawable.ic_drop_down_arrow);
                }else{
                    navView.getMenu().clear();
                    navView.inflateMenu(R.menu.activity_main_user_authorized_actions);
                    arrow.setBackgroundResource(R.drawable.ic_drop_up_arrow);
                }
                mUserSignInInfoExpanded = !mUserSignInInfoExpanded;
            }
        });
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

        final IntentFilter serviceActiveFilter = new IntentFilter();
        serviceActiveFilter.addAction(Utils.ACTION_UPDATE_MAIN_ACTIVITY);
        mServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;

                if (intent.getAction().equals(Utils.ACTION_UPDATE_MAIN_ACTIVITY)) {
                    ArrayList<ShoppingList> loadedShoppingLists = intent.getParcelableArrayListExtra(EXTRAS_KEYS.SHOPPING_LISTS.getValue());
                    if (loadedShoppingLists != null) {
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
        for (ShoppingList newShoppingList : loadedShoppingLists) {
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
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Получаем только имена и идентификаторы списков
        mShoppingLists.clear();
        while (data.moveToNext()) {
            ShoppingList newShoppingList = new ShoppingList(data);
            mShoppingLists.add(newShoppingList);
        }

        if (Utils.sortShoppingListsInReverseOrder(this)) {
            // Отсортируем список, чтобы вверху был последним добавленный список
            Collections.sort(mShoppingLists, new Comparator<IListItem>() {
                @Override
                public int compare(IListItem lhs, IListItem rhs) {
                    return compare(rhs.getId(), lhs.getId());
                }

                private int compare(long x, long y) {
                    return (x < y) ? -1 : ((x == y) ? 0 : 1);
                }
            });
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
    public void onDialogNegativeClick() {
    }

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
                intent.setData(Uri.parse(getString(R.string.market_app_link)));
                startActivity(intent);
                break;
            }
            case R.id.action_sign_in:
            case R.id.action_profile: {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_friends: {
                if (FirebaseUtil.userSignedIn(this)) {
                    Intent intent = new Intent(this, FriendsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.action_black_list: {
                if (FirebaseUtil.userSignedIn(this)) {
                    Intent intent = new Intent(this, BlackListActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.action_receive_shopping_lists: {
                if (FirebaseUtil.userSignedIn(this)) {
                    receiveShoppingListsFromFirebase();
                }
                break;
            }
            case R.id.action_invite_friends: {
                Utils.sendInvitation(this, getString(R.string.invitation_message), REQUEST_INVITE);
                break;
            }
        }
    }

    private void receiveShoppingListsFromFirebase() {
        FirebaseUtil.restartServiceToReceiveShoppingListsFromFirebase(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    private class AsyncTaskDownloadEmail extends AsyncTask<EmailReceiver, Integer, ArrayList<ShoppingList>> {
        @Override
        protected ArrayList<ShoppingList> doInBackground(EmailReceiver... params) {

            ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();

            if (params.length <= 0) return loadedShoppingLists;

            try {
                EmailReceiver receiver = params[0];
                ArrayList<String> fileNames = receiver.getShoppingListsJSONFilesFromEmails();

                // Загружаем новый списков покупок
                for (String fileName : fileNames) {
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
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_emails_for_loading),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Проверим сколько приглашений было отправлено
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }
}

package com.RightDirection.ShoppingList.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.MainActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.models.UserData;
import com.RightDirection.ShoppingList.utils.FirebaseObservables;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;

import static com.RightDirection.ShoppingList.utils.AppLifecycleHandler.isApplicationInForeground;

// Не используем IntentService, чтобы преркащать работу сервиса самостоятельно. Иначе сервис запустит выполнение Observable и прекратится не дождавшись окончания.
public class ExchangeService extends Service {
    private static final String TAG = "ReceiveShoppingLists";
    private static boolean mNotifySourceActivity = false;
    private Disposable mSubscriber;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null)
            mNotifySourceActivity = intent.getBooleanExtra(EXTRAS_KEYS.NOTIFY_SOURCE_ACTIVITY.getValue(), false);

        Observable<UserData> userDataObservable = Observable.zip(
                FirebaseObservables.shoppingListsObservable(),
                FirebaseObservables.friendsObservable(),
                FirebaseObservables.blackListsObservable(),
                new Function3<ArrayList<FirebaseShoppingList>, ArrayList<User>, ArrayList<User>, UserData>() {
                    @Override
                    public UserData apply(ArrayList<FirebaseShoppingList> shoppingLists, ArrayList<User> friends, ArrayList<User> blackList) {
                        return new UserData(shoppingLists, friends, blackList);
                    }
                })
                .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());
        mSubscriber = userDataObservable.subscribe(
                new Consumer<UserData>() {
                    @Override
                    public void accept(UserData userData) {
                        if (userData == null) return;

                        // Переменная для хранения успешно загруженных списков покупок
                        ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();
                        // Переменная для хранения списков, которые необходимо удалить
                        ArrayList<FirebaseShoppingList> shoppingListsForDelete = new ArrayList<>();
                        // Массив пользователей, которых нет ни в друзьях, ни в черном списке, и по которым
                        // необходимо задать вопрос
                        ArrayList<User> unknownUsers = new ArrayList<>();

                        ArrayList<FirebaseShoppingList> fbShoppingLists = userData.getShoppingLists();
                        Context context = getApplicationContext();
                        for (FirebaseShoppingList fbList : fbShoppingLists) {

                            User author = fbList.getAuthor();
                            if (author == null) continue;

                            // Попробуем найти автора в черном списке.
                            // Если автор в черном списке, то просто игнорируем его список покупок.
                            if (isAuthorInBlackList(userData.getBlackList(), author)) {
                                shoppingListsForDelete.add(fbList);
                                continue;
                            }

                            // Автор не в черном списке. Попробуем найти его среди друзей.
                            // Если автор не в друзьях, то прежде чем загрузить список, необходимо
                            // задать вопрос пользователю (и добавить в друзья в случае согласия).
                            // Если автор в друзьях, просто загружаем его список в локальную БД.
                            if (!isAuthorInFriends(userData.getFriends(), author)) {
                                if (!isAuthorInList(unknownUsers, author)) unknownUsers.add(author);
                                // Удалять данный список покупок не надо.
                                continue;
                            }

                            // Сформируем имя нового списка покупок
                            String authorInfo = " " + context.getString(R.string.from) + " " + author.getName();
                            Calendar calendar = Calendar.getInstance();
                            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
                            String newListName = fbList.getName()
                                    + authorInfo
                                    + ", " + dateFormat.format(calendar.getTime());

                            // Создадим  новый объект-лист покупок
                            ShoppingList newShoppingList = new ShoppingList(-1, newListName);
                            newShoppingList.loadProductsFromString(context, fbList.getContent());
                            newShoppingList.addNotExistingProductsToDB(context);
                            // Сначала нужно добавить новые продукты из списка в базу данных.
                            // Синхронизацияя должна производиться по полю Name
                            newShoppingList.addNotExistingProductsToDB(context);
                            // Сохраним новый лист покупок в базе данных
                            newShoppingList.addToDB(context);
                            loadedShoppingLists.add(newShoppingList);
                            shoppingListsForDelete.add(fbList);
                        }

                        if (shoppingListsForDelete.size() > 0)
                            FirebaseUtil.removeCurrentUserShoppingListsFromFirebase(shoppingListsForDelete);

                        if (loadedShoppingLists.size() > 0) {
                            if (!isApplicationInForeground()) {
                                postNotification(getString(R.string.received_new_shopping_lists_summary), getLoadedShoppingListsNamesString(loadedShoppingLists));
                            } else {
                                sendUpdateMainActivityBroadcast(loadedShoppingLists);
                            }
                        } else {
                            sendNotificationBroadcast(getString(R.string.no_shoppping_for_loading));
                        }

                        // Отправим сообщения по пользователям, которых нет ни в одном из списков
                        for (User user : unknownUsers) {
                            if (!isApplicationInForeground()) {
                                postNotification(getString(R.string.message_from_user_not_in_friends), getString(R.string.message_from_user_not_in_friends));
                            } else {
                                sendAddUserToFriendsBroadcast(user);
                            }
                        }

                        stopSelf();
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        if (e instanceof TimeoutException) {
                            Log.d(TAG, "TimeoutException");
                            sendNotificationBroadcast(getString(R.string.connection_timeout_exceeded));
                        }
                        stopSelf();
                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isAuthorInList(ArrayList<User> list, User author) {

        if (list == null) return false;

        for (User user : list) {
            if (user.getUid().equals(author.getUid())) {
                return true;
            }
        }

        return false;
    }

    private boolean isAuthorInBlackList(ArrayList<User> blackList, User author) {
        return isAuthorInList(blackList, author);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAuthorInFriends(ArrayList<User> friends, User author) {
        return isAuthorInList(friends, author);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriber.dispose();
        Log.d(TAG, "onDestroy");
    }

    private void postNotification(String notificationSummary, String notificationText) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setStyle(new NotificationCompat.BigTextStyle(builder)
                .bigText(notificationText)
                .setSummaryText(notificationSummary));
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(notificationSummary)
                .setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    private String getLoadedShoppingListsNamesString(ArrayList<ShoppingList> loadedShoppingLists) {
        String msg = getString(R.string.received_new_shopping_lists) + "\n";
        for (ShoppingList list : loadedShoppingLists) {
            msg += list.getName() + ",\n";
        }
        // Уберем последнюю запятую и символ переноса
        msg = msg.substring(0, msg.length() - 2);
        return msg;
    }

    private void sendUpdateMainActivityBroadcast(ArrayList<ShoppingList> loadedShoppingLists) {
        Intent intent = new Intent();
        intent.setAction(Utils.ACTION_UPDATE_MAIN_ACTIVITY);
        intent.putExtra(EXTRAS_KEYS.SHOPPING_LISTS.getValue(), loadedShoppingLists);

        this.sendBroadcast(intent);
    }

    private void sendNotificationBroadcast(String notification) {
        if (!mNotifySourceActivity) return;

        Intent intent = new Intent();
        intent.setAction(Utils.ACTION_NOTIFICATION);
        intent.putExtra(EXTRAS_KEYS.NOTIFICATION.getValue(), notification);

        this.sendBroadcast(intent);
    }

    private void sendAddUserToFriendsBroadcast(User author) {
        Intent intent = new Intent();
        intent.setAction(Utils.ACTION_ADD_USER_TO_FRIENDS);
        intent.putExtra(EXTRAS_KEYS.AUTHOR.getValue(), author);
        this.sendBroadcast(intent);
    }
}

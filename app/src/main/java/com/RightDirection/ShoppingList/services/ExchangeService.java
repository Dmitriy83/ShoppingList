package com.RightDirection.ShoppingList.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
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
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.TimeoutControl;
import com.RightDirection.ShoppingList.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.functions.Function3;

import static com.RightDirection.ShoppingList.utils.AppLifecycleHandler.isApplicationInForeground;

public class ExchangeService extends IntentService {
    private static final String TAG = "ReceiveShoppingLists";
    private static boolean mNotifySourceActivity = false;
    private TimeoutControl mTimeoutControl;

    public ExchangeService() {
        super("");
    }

    public ExchangeService(String name) {
        super(name);
    }

    private Observable<ArrayList<User>> fbFriendsObservable(){
        return Observable.create(new ObservableOnSubscribe<ArrayList<User>>() {
                    @Override
                    public void subscribe(ObservableEmitter<ArrayList<User>> emitter) throws Exception { addFBListenerToReceiveFriends(emitter); }
                });
    }

    private Observable<ArrayList<User>> fbBlackListsObservable(){
        return Observable.create(new ObservableOnSubscribe<ArrayList<User>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<User>> emitter) throws Exception { addFBListenerToReceiveBlackList(emitter); }
        });
    }

    private Observable<ArrayList<FirebaseShoppingList>> fbShoppingListsObservable(){
        return Observable.create( new ObservableOnSubscribe<ArrayList<FirebaseShoppingList>>() {
                    @Override
                    public void subscribe(ObservableEmitter<ArrayList<FirebaseShoppingList>> emitter) throws Exception { addFBListenerToReceiveShoppingLists(emitter); }
                });
    }

    private void addFBListenerToReceiveFriends(final ObservableEmitter<ArrayList<User>> emitter) {
        DatabaseReference friendsRef = FirebaseUtil.getFriendsRef();
        if (friendsRef == null) {
            stopSelf();
            return;
        }

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTimeoutControl.stop();
                Log.d(TAG, "onDataChange, app in foreground = " + isApplicationInForeground());
                emitter.onNext(receiveFriendsFromFirebase(dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                mTimeoutControl.stop();
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                        Log.d(TAG, "onCancelled");
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private void addFBListenerToReceiveBlackList(final ObservableEmitter<ArrayList<User>> emitter) {
        DatabaseReference blackListRef = FirebaseUtil.getBlackListRef();
        if (blackListRef == null) {
            stopSelf();
            return;
        }

        blackListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTimeoutControl.stop();
                Log.d(TAG, "onDataChange, app in foreground = " + isApplicationInForeground());
                emitter.onNext(receiveBlackListFromFirebase(dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                mTimeoutControl.stop();
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                        Log.d(TAG, "onCancelled");
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private void addFBListenerToReceiveShoppingLists(final ObservableEmitter<ArrayList<FirebaseShoppingList>> emitter){
        DatabaseReference shoppingListsRef = FirebaseUtil.getShoppingListsRef();
        if (shoppingListsRef == null) {
            stopSelf();
            return;
        }

        shoppingListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTimeoutControl.stop();
                Log.d(TAG, "onDataChange, app in foreground = " + isApplicationInForeground());
                emitter.onNext(FirebaseUtil.getShoppingListsFromFB(getApplicationContext(), dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                mTimeoutControl.stop();
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                        Log.d(TAG, "onCancelled");
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private ArrayList<User> receiveFriendsFromFirebase(DataSnapshot dataSnapshot) {
        ArrayList<User> friends = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            User friend = childDataSnapshot.getValue(User.class);
            friend.setUid(childDataSnapshot.getKey());
            friends.add(friend);
        }
        return friends;
    }

    private ArrayList<User> receiveBlackListFromFirebase(DataSnapshot dataSnapshot) {
        ArrayList<User> blackList = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            User user = childDataSnapshot.getValue(User.class);
            user.setUid(childDataSnapshot.getKey());
            blackList.add(user);
        }
        return blackList;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (intent != null)
            mNotifySourceActivity = intent.getBooleanExtra(EXTRAS_KEYS.NOTIFY_SOURCE_ACTIVITY.getValue(), false);

        mTimeoutControl = new TimeoutControl();
        mTimeoutControl.addListener(new TimeoutControl.IOnTimeoutListener() {
            @Override
            public void onTimeout() { sendNotificationBroadcast(getString(R.string.connection_timeout_exceeded)); }
        });
        mTimeoutControl.start();

        Observable<UserData> userDataObservable = Observable.zip(fbShoppingListsObservable(), fbFriendsObservable(), fbBlackListsObservable(),
                new Function3<ArrayList<FirebaseShoppingList>, ArrayList<User>, ArrayList<User>, UserData>() {
                    @Override
                    public UserData apply(ArrayList<FirebaseShoppingList> shoppingLists, ArrayList<User> friends, ArrayList<User> blackList) throws Exception { return new UserData(shoppingLists, friends, blackList); }
        });
        userDataObservable
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<UserData>() {
                    @Override
                    public void accept(UserData userData) throws Exception {
                        // Переменная для хранения успешно загруженных списков покупок
                        ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();
                        // Переменная для хранения списков, которые необходимо удалить
                        ArrayList<FirebaseShoppingList> shoppingListsForDelete = new ArrayList<>();

                        ArrayList<FirebaseShoppingList> fbShoppingLists = userData.getShoppingLists();
                        Context context = getApplicationContext();
                        for (FirebaseShoppingList fbList: fbShoppingLists) {

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
                            if (!isAuthorInFriends(userData.getFriends(), author)){
                                // TODO: Реализовать функционал отправки сообщения о добавлении в друзья.
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
                    }
                });
    }

    private boolean isAuthorInList(ArrayList<User> list, User author) {

        if (list == null) return false;

        for (User user: list) {
            if (user.getUid().equals(author.getUid())){
                return true;
            }
        }

        return false;
    }

    private boolean isAuthorInBlackList(ArrayList<User> blackList, User author) {
        return isAuthorInList(blackList, author);
    }

    private boolean isAuthorInFriends(ArrayList<User> friends, User author) {
        return isAuthorInList(friends, author);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
}

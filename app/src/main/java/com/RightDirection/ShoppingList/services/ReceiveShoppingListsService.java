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
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.TimeoutControl;
import com.RightDirection.ShoppingList.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.RightDirection.ShoppingList.utils.AppLifecycleHandler.isApplicationInForeground;

public class ReceiveShoppingListsService extends IntentService {
    private static final String TAG = "ReceiveShoppingLists";
    private static boolean mNotifySourceActivity = false;

    public ReceiveShoppingListsService() {
        super("");
    }

    public ReceiveShoppingListsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");

        DatabaseReference shoppingListsRef = FirebaseUtil.getShoppingListsRef();
        if (shoppingListsRef == null) {
            stopSelf();
            return;
        }

        if (intent != null)
            mNotifySourceActivity = intent.getBooleanExtra(EXTRAS_KEYS.NOTIFY_SOURCE_ACTIVITY.getValue(), false);


        final TimeoutControl timeoutControl = new TimeoutControl();
        timeoutControl.addListener(new TimeoutControl.IOnTimeoutListener() {
            @Override
            public void onTimeout() {
                sendNotificationBroadcast(getString(R.string.connection_timeout_exceeded));
            }
        });
        timeoutControl.start();

        shoppingListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timeoutControl.stop();
                Log.d(TAG, "onDataChange, app in foreground = " + isApplicationInForeground());
                receiveShoppingListsFromFirebase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                timeoutControl.stop();
                System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                Log.d(TAG, "onCancelled");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void postNotification(String notificationString) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_products)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(notificationString);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void receiveShoppingListsFromFirebase(DataSnapshot dataSnapshot) {

        ArrayList<ShoppingList> loadedShoppingLists = FirebaseUtil.loadShoppingLists(this, dataSnapshot);

        // В случае, успешной загрузки и работе приложения в foreground оповестим адаптер об изменении
        if (loadedShoppingLists.size() > 0) {
            // Все загруженные листы следует удалить
            FirebaseUtil.removeCurrentUserShoppingListsFromFirebase();

            if (!isApplicationInForeground()) {
                postNotification(getLoadedShoppingListsNamesString(loadedShoppingLists));
            } else {
                sendUpdateMainActivityBroadcast(loadedShoppingLists);
            }
        }else{
            sendNotificationBroadcast(getString(R.string.no_shoppping_for_loading));
        }
    }

    private String getLoadedShoppingListsNamesString(ArrayList<ShoppingList> loadedShoppingLists) {
        String msg = getString(R.string.received_new_shopping_lists) + " ";
        for (ShoppingList list: loadedShoppingLists) {
            msg += list.getName() + ",";
        }
        msg = msg.substring(0, msg.length() - 1);
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

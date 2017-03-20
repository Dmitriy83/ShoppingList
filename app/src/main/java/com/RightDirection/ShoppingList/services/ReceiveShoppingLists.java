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
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.RightDirection.ShoppingList.utils.AppLifecycleHandler.isApplicationInForeground;

public class ReceiveShoppingLists extends Service {
    private static final String TAG = "ReceiveShoppingLists";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service was started");

        ValueEventListener handler = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange, app in foreground = " + isApplicationInForeground());
                receiveShoppingListsFromFirebase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        };

        FirebaseUtil.getCurrentUserRef().child(FirebaseUtil.getShoppingListsPath())
                .addValueEventListener(handler);
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

        ArrayList<FirebaseShoppingList> firebaseLists = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            FirebaseShoppingList firebaseShoppingList = childDataSnapshot.getValue(FirebaseShoppingList.class);
            firebaseShoppingList.setName(childDataSnapshot.getKey());
            firebaseLists.add(firebaseShoppingList);
        }

        // Загружаем новый списков покупок
        ArrayList<ShoppingList> loadedShoppingLists = new ArrayList<>();
        for (FirebaseShoppingList firebaseList : firebaseLists) {
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

        // В случае, успешной загрузки и работе приложения в foreground оповестим адаптер об изменении
        if (loadedShoppingLists.size() > 0) {
            // Все загруженные листы следует удалить
            FirebaseUtil.getCurrentUserRef()
                    .child(FirebaseUtil.getShoppingListsPath()).removeValue();

            if (!isApplicationInForeground()) {
                postNotification("Received new shopping lists.");
            }else{
                sendUpdateMainActivityBroadcast(loadedShoppingLists);
            }
        }
    }

    private void sendUpdateMainActivityBroadcast(ArrayList<ShoppingList> loadedShoppingLists) {
        final Intent intent = new Intent();
        intent.setAction(Utils.ACTION_UPDATE_MAIN_ACTIVITY);
        intent.putExtra("shoppingLists", loadedShoppingLists);

        this.sendBroadcast(intent);
    }
}

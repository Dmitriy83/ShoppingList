package com.RightDirection.ShoppingList.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Срабатывает с заданной периодичностью по AlarmManager
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, ExchangeService.class);
        context.startService(newIntent);
    }
}

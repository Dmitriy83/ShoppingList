package com.RightDirection.ShoppingList.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Срабатывает с заданной периодичностью по AlarmManager
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, ExchangeService.class);
        // Если сервис был запущен до этого вручную, не будем его останавливать и не будем стартовать новый
        if (!isServiceRunning(ExchangeService.class, context)){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                context.startForegroundService(newIntent);
            } else{
                context.startService(newIntent);
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}


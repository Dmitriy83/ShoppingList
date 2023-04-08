package com.RightDirection.ShoppingList.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Срабатывает с заданной периодичностью по AlarmManager
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, ExchangeService.class);
        // Если сервис был запущен до этого вручную, не будем его останавливать и не будем стартовать новый
        if (!isServiceRunning(ExchangeService.class, context)){
            //TODO: Sole issue with starting of foreground service on SDK older then 31
            /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ExchangeWorker.class).addTag ("EXCHANGE_WORKER_TAG").build();
                WorkManager.getInstance(context).enqueue(request);
            } else */

            // Quick fix of ForegroundServiceStartNotAllowedException for SDK >= 31
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(newIntent);
                } else {
                    context.startService(newIntent);
                }
            } catch(Exception e) {
                Log.e("IO","IO" + e);
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


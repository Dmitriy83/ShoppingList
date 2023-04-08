package com.RightDirection.ShoppingList.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//TODO: Sole issue with starting of foreground service on SDK older then 31
public class ExchangeWorker extends Worker {

    private static final String TAG = "EXCHANGE_WORKER_TAG";

    public ExchangeWorker (@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super (context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork () {
        //call methods to perform background task
        return Result.success();
    }
}

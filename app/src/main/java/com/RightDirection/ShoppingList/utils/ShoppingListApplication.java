package com.RightDirection.ShoppingList.utils;

import android.app.Application;

public class ShoppingListApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Добавим обработчик жизненного цикла приложения
        registerActivityLifecycleCallbacks(new AppLifecycleHandler());
    }
}

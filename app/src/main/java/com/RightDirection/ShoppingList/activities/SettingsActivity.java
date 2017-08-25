package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.fragments.SettingsFragment;


public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Отобразим фрагмент в качетсве основного контента
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Установим настройки по умолчанию
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
}


package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;

import com.RightDirection.ShoppingList.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ShowHelpCheckBoxPreference extends CustomCheckBoxPreference {
    public ShowHelpCheckBoxPreference(Context context) {
        super(context);
    }

    public ShowHelpCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowHelpCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ShowHelpCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        super.onClick();

        // Изменим связанную настройку
        SharedPreferences settings = getDefaultSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(getContext().getString(R.string.pref_key_show_help_in_main_activity), isChecked());
        editor.apply();
    }
}

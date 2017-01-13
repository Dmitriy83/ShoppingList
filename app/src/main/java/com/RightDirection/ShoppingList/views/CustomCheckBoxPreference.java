package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

@SuppressWarnings("WeakerAccess") // Если сделать package local, то возникает ошибка при открытии настроек
class CustomCheckBoxPreference extends CheckBoxPreference {

    public CustomCheckBoxPreference(Context context) {
        super(context);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setTextColor(Color.BLACK);
    }
}

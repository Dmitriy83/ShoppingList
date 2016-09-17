package com.RightDirection.ShoppingList.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.RightDirection.ShoppingList.R;

public class CustomEditTextPreferenceForPassword extends EditTextPreference{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomEditTextPreferenceForPassword(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreferenceForPassword(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreferenceForPassword(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreferenceForPassword(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        // Добавим возможность установки в аннотацию значения настройки.
        // Установка значения производится с помощью тега %s в свойстве summary в xml
        //String summary = super.getSummary().toString();
        String password = getText();
        if (password == null || password.isEmpty()){
            return getContext().getString(R.string.not_assigned);
        } else{
            return getContext().getString(R.string.password_mask);
        }
    }
}

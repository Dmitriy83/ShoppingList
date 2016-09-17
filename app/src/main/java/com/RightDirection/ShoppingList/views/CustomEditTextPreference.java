package com.RightDirection.ShoppingList.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

import com.RightDirection.ShoppingList.R;

public class CustomEditTextPreference extends EditTextPreference{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        // Добавим возможность установки в аннотацию значения настройки.
        // Установка значения производится с помощью тега %s в свойстве summary в xml
        String summary = super.getSummary().toString();
        if (getText() == null || getText().isEmpty()){
            return getContext().getString(R.string.not_assigned);
        } else{
            return String.format(summary, getText());
        }
    }
}

package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Добавлен, чтобы избежать предупреждения Custom view `'ImageButton'` has 'setOnTouchListener' called on it but does not override 'performClick'
 */
public class CustomImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public CustomImageButton(Context context) {
        super(context);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}

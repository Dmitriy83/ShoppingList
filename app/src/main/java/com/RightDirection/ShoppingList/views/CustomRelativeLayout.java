package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Добавлен, чтобы избежать предупреждения Custom view `'ImageButton'` has 'setOnTouchListener' called on it but does not override 'performClick'
 */
public class CustomRelativeLayout extends RelativeLayout {
    public CustomRelativeLayout(Context context) {
        super(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}

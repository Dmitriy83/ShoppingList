package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.RightDirection.ShoppingList.R;

public class SoftKeyboardListnedRelativeLayout extends RelativeLayout {

    public SoftKeyboardListnedRelativeLayout(Context context) {
        super(context);
    }

    public SoftKeyboardListnedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        // Покажем/скроем кнопки в зависимости от того, отображается на экране клавиатура или нет
        Button btnDeleteAllItems = (Button) findViewById(R.id.btnShoppingListDeleteAllItems);
        if (yOld > yNew) {
            // Keyboard is shown
            btnDeleteAllItems.setVisibility(INVISIBLE);
        }
        else {
            // Keyboard is hidden
            btnDeleteAllItems.setVisibility(VISIBLE);
        }
    }
}

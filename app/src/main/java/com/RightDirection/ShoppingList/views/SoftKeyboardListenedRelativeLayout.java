package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.RightDirection.ShoppingList.R;

public class SoftKeyboardListenedRelativeLayout extends RelativeLayout {

    public SoftKeyboardListenedRelativeLayout(Context context) {
        super(context);
    }

    public SoftKeyboardListenedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        // Выполним действия в зависимости от того, отображается на экране клавиатура или нет
        Button btnDeleteAllItems = (Button)findViewById(R.id.btnShoppingListDeleteAllItems);

        if (yOld > yNew) {
            // Keyboard is shown
            if (btnDeleteAllItems != null) {
                //btnDeleteAllItems.setVisibility(INVISIBLE);
            }
        } else {
            // Keyboard is hidden
            if (btnDeleteAllItems != null) {
                //btnDeleteAllItems.setVisibility(VISIBLE);
            }
        }

        setButtonsPanelPadding();
    }

    public void setButtonsPanelPadding(){
        // Выполним действия в зависимости от того, отображается на экране клавиатура или нет
        Button btnSave = (Button)findViewById(R.id.btnShoppingListSave);
        RelativeLayout buttonsPanel = (RelativeLayout)findViewById(R.id.buttonsPanel);
        View shoppingListFragment = findViewById(R.id.frgShoppingList);

        if (buttonsPanel != null && shoppingListFragment != null && btnSave != null
               && (getHeight() - btnSave.getHeight() * 2) < shoppingListFragment.getHeight()) {
            // Сдвинем кнопку левее, чтобы не перекрывала кнопки удаления элементов
            buttonsPanel.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.save_button_margin), 0);
        }
        else {
            if (buttonsPanel != null) {
                // Уберем сдвиг влево
                buttonsPanel.setPadding(0, 0, 0, 0);
            }
        }

        // Чтобы свойства padding устанавливались "вовремя" (объяснение здесь: http://stackoverflow.com/a/6427281/2688351)
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }
}


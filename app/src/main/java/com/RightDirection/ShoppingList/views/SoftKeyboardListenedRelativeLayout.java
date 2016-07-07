package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.RightDirection.ShoppingList.interfaces.IObservable;
import com.RightDirection.ShoppingList.interfaces.IObserver;

import java.util.ArrayList;
import java.util.List;

public class SoftKeyboardListenedRelativeLayout extends RelativeLayout implements IObservable{

    public SoftKeyboardListenedRelativeLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        observers = new ArrayList<>();
    }

    public SoftKeyboardListenedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        observers = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        /*
        // Выполним действия в зависимости от того, отображается на экране клавиатура или нет
        Button btnDeleteAllItems = (Button)findViewById(R.id.btnShoppingListDeleteAllItems);

        if (yOld > yNew) {
            // Keyboard is shown
            if (btnDeleteAllItems != null) {
                btnDeleteAllItems.setVisibility(INVISIBLE);
            }
        } else {
            // Keyboard is hidden
            if (btnDeleteAllItems != null) {
                btnDeleteAllItems.setVisibility(VISIBLE);
            }
        }
        */
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Оповестим наблюдателей о том, что отрисовка контейнера завершена
        notifyObservers();
    }

    private List<IObserver> observers;

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (IObserver observer : observers)
            observer.layoutWasDrawed();
    }
}


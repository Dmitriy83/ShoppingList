package com.RightDirection.ShoppingList.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
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

    /**
    Реализуем интерфейс/шаблон Наблюдатель
    */
    protected List<IObserver> observers;

    @Override
    public void addObserver(IObserver observer) {
        if (observer != null) observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        if (observer != null) observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (IObserver observer : observers)
            observer.layoutWasDrawed();
    }
}


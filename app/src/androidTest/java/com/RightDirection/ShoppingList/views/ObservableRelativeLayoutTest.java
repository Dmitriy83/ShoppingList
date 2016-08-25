package com.RightDirection.ShoppingList.views;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.SmallTest;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.interfaces.IObserver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class ObservableRelativeLayoutTest {

    private ObservableRelativeLayout mRelativeLayout;

    @Mock IObserver mObserver;

    @Rule
    public ActivityTestRule<ShoppingListEditingActivity> mActivityRule = new ActivityTestRule<>(
            ShoppingListEditingActivity.class);

    @Before
    public void setUp() throws Exception {
        Activity activity = mActivityRule.getActivity();
        mRelativeLayout = (ObservableRelativeLayout)activity.findViewById(R.id.shoppingListEditingContainerLayout);
        mObserver = new IObserver() {
            @Override
            public void layoutHasDrawn() {

            }
        };
    }

    @Test
    @SmallTest
    public void testAddObserver() throws Exception {
        mRelativeLayout.addObserver(mObserver);
        assertEquals(2, mRelativeLayout.observers.size());
    }

    @Test
    @SmallTest
    public void testRemoveObserver() throws Exception {
        // Протестируем удаление из списка отсутствующего элемента
        mRelativeLayout.removeObserver(mObserver);
        assertEquals(1, mRelativeLayout.observers.size());

        // Добавим новый элемент, чтобы потом удалить его
        mRelativeLayout.addObserver(mObserver);
        assertEquals(2, mRelativeLayout.observers.size());

        // Удалим и проверим, что в списке нет элементов
        mRelativeLayout.removeObserver(mObserver);
        assertEquals(1, mRelativeLayout.observers.size());
    }

    @Test
    @SmallTest
    public void testNotifyObservers() throws Exception {
        mRelativeLayout.notifyObservers();

        mRelativeLayout.addObserver(mObserver);
        mRelativeLayout.notifyObservers();

        mRelativeLayout.removeObserver(mObserver);
        mRelativeLayout.addObserver(null);
        mRelativeLayout.notifyObservers();
    }
}
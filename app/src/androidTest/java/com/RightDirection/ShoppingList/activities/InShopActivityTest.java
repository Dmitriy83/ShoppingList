package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.filters.MediumTest;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;

public class InShopActivityTest extends ActivitiesTest {

    @Test
    @MediumTest
    public void testActivityInShop() {
        addNewShoppingList();
        editNewShoppingList();

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean crossOutProduct = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_cross_out_action), true);
        if (!crossOutProduct) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_cross_out_action))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }

        // Клик на новом списке покупок -> Переход к активности "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());

        // Пробуем вычеркнуть товары обычным нажатие. Проверяем, что окно "Победа!" не отобразилось
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(click());
        }
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(doesNotExist());

        // Вычеркиванием все товары
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(swipeRight());
        }
        // Проверяем, что появилось окно с надписью "Победа!"
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));

        // Возвращаемся к основной активности
        pressBack();
        pressBack();

        // Заходим в Настройки
        openSettings();

        // Меняем настроку "вычеркивания"
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).perform(click());

        // Возвращаемся к основной активности
        pressBack();

        // Клик на новом списке покупок -> Переход к активности "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());

        // Вычеркиванием все товары
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(click());
        }
        // Проверяем, что появилось окно с надписью "Победа!"
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));

        // Возвращаемся к основной активности
        pressBack();
        pressBack();
    }

    @Test
    @MediumTest
    public void inShopActivity_SendEmail() throws UiObjectNotFoundException {
        addNewShoppingList();

        // Переходим в активность "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());

        // Нажимаем кнопку отправки списка покупок по почте
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(mActivity.getString(R.string.send_by_email))).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        // Проверяем, что в теле письма правильно представлен список
        UiObject emailBody = mDevice.findObject(new UiSelector().text("" + mNewProductNamePattern + "1, 1.0;" + "\n" + mNewProductNamePattern + "2, 1.0;"));
        assertTrue(emailBody.exists());
        mDevice.pressBack();
        mDevice.pressBack();
    }

    @Test
    @MediumTest
    public void inShopActivity_LoadShoppingList(){
        addNewShoppingList();

        // Клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(click());

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // В меню нажимаем кнопку отправки списка по почте
        onView(withText(R.string.load)).perform(click());

        // Открылась форма загрузки
        loadAndCheckList();
    }
}
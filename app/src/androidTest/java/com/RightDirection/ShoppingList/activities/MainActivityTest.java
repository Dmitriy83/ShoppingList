package com.RightDirection.ShoppingList.activities;

import android.os.Build;
import android.support.test.filters.MediumTest;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.utils.Utils;

import org.junit.Ignore;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;

public class MainActivityTest extends ActivitiesTest {

    @Test
    @MediumTest
    public void testAddAndEditNewShoppingList(){
        addNewShoppingList();
        editNewShoppingList();
    }

    @Test
    @MediumTest
    public void testRenameShoppingList() throws UiObjectNotFoundException {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        //openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // Espresso вызывает ошибку при открытии меню, т.к. находит 2 меню: ActionMode и ActionBar
        UiObject btnMenu = mDevice.findObject(new UiSelector().description(mActivity.getString(R.string.menu_button_identifier)));
        btnMenu.click();
        onView(withText(mActivity.getString(R.string.change_list_name))).perform(click());

        // Вводим новое имя списка покупок
        mNewListName += "Changed";
        onView(withId(R.id.inputNewListName)).perform(clearText());
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверяем, что в списке отображен переименованный элемент
        onView(recyclerViewItemWithText(mNewListName)).check(matches(isDisplayed()));
    }

    @Test
    @Ignore
    @MediumTest
    public void testSendReceiveEmailFromMainActivity() throws UiObjectNotFoundException {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку отправки списка по почте
        onView(withId(R.id.action_send_by_email)).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        UiObject emailAttachments = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'.json"));
        assertTrue(emailAttachments.exists());
        UiObject btnSend = mDevice.findObject(new UiSelector().description(mActivity.getString(R.string.send)));
        btnSend.click();

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Подождем 5 секунд, чтобы письмо появилось на сервере
        timeout(5000);

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_receive_shopping_list_by_email))).perform(click());

        // Проверим появление загруженного списка
        onView(recyclerViewItemContainsText(mActivity.getString(R.string.loaded))).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testDeleteShoppingList() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        // В меню действий нажимаем кнопку удаления списка
        onView(withId(R.id.imgDelete)).perform(click());
        // Проверяем, что открылось окно с вопросом об удалении списка
        onView(withText(mActivity.getString(R.string.delete_shopping_list_question))).check(matches(isDisplayed()));
        // Отклоняем удаление
        onView(withText(mActivity.getString(R.string.cancel))).perform(click());
        // Проверяем, что список покупок не исчез
        onView(recyclerViewItemWithText(mNewListName)).check(matches(isDisplayed()));

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        // В меню действий нажимаем кнопку удаления списка
        onView(withId(R.id.imgDelete)).perform(click());
        // Проверяем, что открылось окно с вопросом об удалении списка
        onView(withText(mActivity.getString(R.string.delete_shopping_list_question))).check(matches(isDisplayed()));
        // Подтверждаем удаление
        onView(withText(mActivity.getString(R.string.ok))).perform(click());
        // Проверяем, что список покупок исчез
        // Вариант из https://google.github.io/android-testing-support-library/docs/espresso/advanced/#asserting-that-a-view-is-not-present
        // не подходит для ListFragment. На вопрос http://stackoverflow.com/questions/39015672/how-to-assert-that-a-data-item-is-not-in-a-listfragment-with-espresso
        // ответа не получил. Поэтому реализовал свой метод для проверки.
        checkDataNotExistInList(mNewListName);
    }

    @Test
    @MediumTest
    public void testOpenSettings() {
        openSettings();
    }

    @Test
    @MediumTest
    public void mainActivity_SendEmail() throws UiObjectNotFoundException{
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку отправки списка по почте
        onView(withId(R.id.action_send_by_email)).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        // Проверяем, что в теле письма правильно представлен список
        UiObject emailBody = mDevice.findObject(new UiSelector().text("" + mNewProductNamePattern + "2, 1.0;" + "\n" + mNewProductNamePattern + "1, 1.0;"));
        assertTrue(emailBody.exists());
        //UiObject btnSend = mDevice.findObject(new UiSelector().description(mActivity.getString(R.string.send)));
        //btnSend.click();
        mDevice.pressBack();
        mDevice.pressBack();
    }

    @Test
    @MediumTest
    public void mainActivity_LoadShoppingList(){
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку отправки списка по почте
        onView(withId(R.id.action_load_list)).perform(click());

        // Открылась форма загрузки
        loadAndCheckList();
    }

    @Test
    @MediumTest
    public void feedback(){
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(mActivity.getString(R.string.feedback))).perform(click());
        // Скроем клавиатуру
        mDevice.pressBack();
        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.feedback_email)));
        assertTrue(emailSubject.exists());
        // Проверяем, что в теле письма правильно представлен список
        UiObject emailBody = mDevice.findObject(new UiSelector().text("\n" + "\n" + "\n" + "\n"
                + mActivity.getString(R.string.email_body_divider) + "\n" + Utils.getDeviceName()
                + "\nAndroid " + Build.VERSION.RELEASE));
        assertTrue(emailBody.exists());
        mDevice.pressBack();
        mDevice.pressBack();
    }
}
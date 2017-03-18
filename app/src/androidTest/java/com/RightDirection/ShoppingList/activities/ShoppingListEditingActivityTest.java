package com.RightDirection.ShoppingList.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.test.filters.MediumTest;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.lessThan;

public class ShoppingListEditingActivityTest extends ActivitiesTest {

    @Test
    @MediumTest
    public void testShoppingListEditingActivity_ProductsCountEnter() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // На одном из элементов проверяем, что количество = 1
        //onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
        //        .check(matches(withText("1.0")));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .check(matches(withText("1.0")));


        // Нажимаем два раза на кнопку "Increase"
        //onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.imgIncrease))
        onView(allOf(withId(R.id.imgIncrease),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(click())
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .check(matches(withText("3.0")));

        // Нажимаем кнопку "Decrease"
        onView(allOf(withId(R.id.imgDecrease),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 2
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .check(matches(withText("2.0")));

        // Вводим в текстовое поле количество 5
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(clearText())
                .perform(typeText("5.0"));

        // Попытаемся ввести не число
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(typeText("Not number"))
                // Проверяем, что в окне снова отображается 5
                .check(matches(withText("5.0")));

        // Выбираем другой элемент списка. С помощью кнопки добавим количество до 11
        for (int i=0; i<10; i++) {
            onView(allOf(withId(R.id.imgIncrease),
                    withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "2")))))
                    .perform(click());
        }

        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .check(matches(withText("5.0")));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "2")))))
                .check(matches(withText("11.0")));

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверяем, что в таблице содержимого списка покупок для редактированных элементов проставлено корректное количество
        ContentResolver contentResolver = mActivity.getContentResolver();
        Cursor cursor = contentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, SL_ContentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "1' OR "
                        + SL_ContentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "2'",
                null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 2);
        int keyCountIndex = cursor.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT);
        int keyNameIndex = cursor.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME);
        while (cursor.moveToNext()) {
            if (cursor.getString(keyNameIndex).equals(mNewProductNamePattern + "1")) {
                assertEquals("5", cursor.getString(keyCountIndex));
            }
            else if (cursor.getString(keyNameIndex).equals(mNewProductNamePattern + "2")) {
                assertEquals("11", cursor.getString(keyCountIndex));
            }
        }
        cursor.close();

        testShoppingListInShopActivity_CountAppearing();

        pressBack();
    }

    private void testShoppingListInShopActivity_CountAppearing() {
        // Клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Проверяем, что открылась активность "В магазине"
        onView(withId(R.id.action_filter)).check(matches(isDisplayed()));


        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onView(allOf(withId(R.id.txtCount),
                hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("5.0")));
        onView(allOf(withId(R.id.txtCount),
                hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "2"))))
                .check(matches(withText("11.0")));
    }

    @Test
    @MediumTest
    public void testSwitchingBetweenInShopAndEditActivity(){
        // Сначала проверим переключение в уже сохраненном списке
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Нажимаем два раза на кнопку "Increase"
        onView(allOf(withId(R.id.imgIncrease),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(click())
                .perform(click());

        // Переключаемся на активность "В магазине"
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        onView(allOf(withId(R.id.txtCount),
                hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("3.0")));

        // Переключаемся на активность "Редактирование списка"
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        pressBack();
        pressBack(); // вернулись к основной активности

        // Проверим работу переключения для несохраненного списка
        // Нажмем на кнопку добавления нового списка покупок
        onView(withId(R.id.fabAddNewShoppingList)).perform(click());

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Плюс"
        String textForTyping = mNewProductNamePattern + "1";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Готово"
        textForTyping = mNewProductNamePattern + "2";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping), pressImeActionButton());

        // Нажимаем два раза на кнопку "Increase"
        onView(allOf(withId(R.id.imgIncrease),
                withParent(hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1")))))
                .perform(click());

        // "Глюк" Espresso - если не закрыть клавиатуру перед вызововм диалгового окна, то
        // Espresso в большинстве случаев не открывает клавиатуру при печати в текстовом поле
        // inputNewListName диалогового окна, однако пытается произвести действия с диалоговым
        // окном в том месте, как будто оно было смещено (видимо, действия производятся
        // по координатам экрана). Из-за этого вместо печати текста в поле диалогового окна,
        // например, может открыться Activity редактирования товара и текст начнет набираться
        // в текстовом поле этой Activity. Принудительное закрытие клавиатуры перед нажатием
        // кнопки сохранения списка решает эту проблему.
        onView(withId(R.id.newItemEditText)).perform(closeSoftKeyboard());

        // Переключаемся на активность "В магазине"
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());

        // Введем имя нового списка
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверяем, что в активности "В магазине" в текстовом поле отображается число 2
        onView(allOf(withId(R.id.txtCount),
                hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("2.0")));

        // Переключаемся на активность "Редактирование списка"
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void shoppingListEditingActivity_SendEmail() throws UiObjectNotFoundException {
        addNewShoppingList();

        // Переходим в активность "Редактирование списка"
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Нажимаем кнопку отправки списка покупок по почте
        onView(withText(mActivity.getString(R.string.share))).perform(click());

        checkEmailAppearing(
                mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'",
                "" + mNewProductNamePattern + "2, 1.0;" + "\n" + mNewProductNamePattern + "1, 1.0;");
    }

    @Test
    @MediumTest
    public void shoppingListEditingActivity_LoadShoppingList(){
        addNewShoppingList();

        // Клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // В меню нажимаем кнопку отправки списка по почте
        onView(withText(R.string.load)).perform(click());

        // Открылась форма загрузки
        loadAndCheckList();
    }

    @Test
    @MediumTest
    public void shoppingListEditingActivity_AddEmptyItem(){
        addNewShoppingList();

        // Открываем для редактирования
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        onView(withId(R.id.rvProducts)).check(new RecyclerViewItemCountAssertion(lessThan(3)));

        pressImeActionButton();
        onView(withId(R.id.rvProducts)).check(new RecyclerViewItemCountAssertion(lessThan(3)));
    }
}
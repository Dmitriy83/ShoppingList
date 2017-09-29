package com.RightDirection.ShoppingList.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.filters.MediumTest;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

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
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;

public class ShoppingListEditingActivityTest extends ActivitiesTest {

    private ViewInteraction getBtnIncreaseViewInteraction(String productName) {
        if (!Utils.showPrices(mActivity)) {
            return onView(allOf(withId(getImgIncreaseId()),
                    withParent(hasSibling(recyclerViewItemWithText(productName)))));
        }else{
            return onView(allOf(withId(getImgIncreaseId()),
                    withParent(withParent(hasSibling(recyclerViewItemWithText(productName))))));
        }
    }

    private ViewInteraction getBtnDecreaseViewInteraction(String productName) {
        if (!Utils.showPrices(mActivity)) {
            return onView(allOf(withId(getImgDecreaseId()),
                    withParent(hasSibling(recyclerViewItemWithText(productName)))));
        }else{
            return onView(allOf(withId(getImgDecreaseId()),
                    withParent(withParent(hasSibling(recyclerViewItemWithText(productName))))));
        }
    }

    private ViewInteraction getTvUnitViewInteraction(String productName) {
        if (!Utils.showPrices(mActivity)) {
            return onView(allOf(withId(getTvUnitId()),
                    withParent(hasSibling(recyclerViewItemWithText(productName)))));
        }else{
            return onView(allOf(withId(getTvUnitId()),
                    withParent(withParent(hasSibling(recyclerViewItemWithText(productName))))));
        }
    }

    private ViewInteraction getImgDeleteViewInteraction(String productName) {
        return onView(allOf(withId(R.id.imgDelete),
                    hasSibling(recyclerViewItemWithText(productName))));
    }

    private ViewInteraction getEtPriceViewInteraction(String productName) {
        if (!Utils.showPrices(mActivity)) {
            return onView(allOf(withId(R.id.etLastPrice),
                    withParent(hasSibling(recyclerViewItemWithText(productName)))));
        } else {
            return onView(allOf(withId(R.id.etLastPrice),
                    withParent(withParent(hasSibling(recyclerViewItemWithText(productName))))));
        }
    }

    private ViewInteraction getRecyclerViewItemTvSumInfoViewInteraction(String listName) {
        return onView(allOf(withId(R.id.tvSumInfo),
                withParent(hasSibling(recyclerViewItemWithText(listName)))));
    }

    @Test
    @LargeTest
    public void testShoppingListEditingActivity_ProductsCountEnter(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(false);
        setSettingsShowPrices(false);
        productsCountEnter();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(true);
        setSettingsShowPrices(true);
        productsCountEnter();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(false);
        setSettingsShowPrices(true);
        productsCountEnter();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(true);
        setSettingsShowPrices(false);
        productsCountEnter();
        removeShoppingList(mNewListName);
    }

    private void removeShoppingList(String listName){
        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(listName)).perform(longClick());
        // В меню действий нажимаем кнопку удаления списка
        onView(withId(R.id.imgDelete)).perform(click());
        // Проверяем, что открылось окно с вопросом об удалении списка
        onView(withText(mActivity.getString(R.string.delete_shopping_list_question))).check(matches(isDisplayed()));
        // Подтверждаем удаление
        onView(withText(mActivity.getString(R.string.ok))).perform(click());
    }
    
    private void productsCountEnter() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // На одном из элементов проверяем, что количество = 1
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .check(matches(withText("1.0")));

        // Нажимаем два раза на кнопку "Increase"
        getBtnIncreaseViewInteraction(mNewProductNamePattern + "1")
                .perform(click())
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .check(matches(withText("3.0")));

        // Нажимаем кнопку "Decrease"
        getBtnDecreaseViewInteraction(mNewProductNamePattern + "1").perform(click());

        // Проверяем, что в текстовом поле отображается число 2
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .check(matches(withText("2.0")));

        // Вводим в текстовое поле количество 5
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .perform(clearText())
                .perform(typeText("5.0"));

        // Попытаемся ввести не число
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .perform(typeText("Not number"))
                // Проверяем, что в окне снова отображается 5
                .check(matches(withText("5.0")));

        // Выбираем другой элемент списка. С помощью кнопки добавим количество до 11
        for (int i=0; i<10; i++) {
            getBtnIncreaseViewInteraction(mNewProductNamePattern + "2")
                    .perform(click());
        }

        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        getEtCountViewInteraction(mNewProductNamePattern + "1")
                .check(matches(withText("5.0")));
        getEtCountViewInteraction(mNewProductNamePattern + "2")
                .check(matches(withText("11.0")));

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверяем, что в таблице содержимого списка покупок для редактированных элементов проставлено корректное количество
        ContentResolver contentResolver = mActivity.getContentResolver();
        Cursor cursor = contentResolver.query(SL_ContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, SL_ContentProvider.KEY_NAME + " = ? OR " + SL_ContentProvider.KEY_NAME + " = ?",
                new String[]{mNewProductNamePattern + "1", mNewProductNamePattern + "2"}, null);
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

        timeout(1000);
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
                .check(matches(withText(containsString("5.0"))));
        onView(allOf(withId(R.id.txtCount),
                hasSibling(recyclerViewItemWithText(mNewProductNamePattern + "2"))))
                .check(matches(withText(containsString("11.0"))));
    }

    @Test
    @LargeTest
    public void testSwitchingBetweenInShopAndEditActivity() {
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(false);
        setSettingsShowPrices(false);
        switchingBetweenInShopAndEditActivity();
        removeShoppingList(mNewListName);
        timeout(1000);
        removeShoppingList(mNewListName + "1");
        setSettingsShowUnits(true);
        setSettingsShowPrices(true);
        switchingBetweenInShopAndEditActivity();
        removeShoppingList(mNewListName);
        timeout(1000);
        removeShoppingList(mNewListName + "1");
        setSettingsShowUnits(false);
        setSettingsShowPrices(true);
        switchingBetweenInShopAndEditActivity();
        removeShoppingList(mNewListName);
        timeout(1000);
        removeShoppingList(mNewListName + "1");
        setSettingsShowUnits(true);
        setSettingsShowPrices(false);
        switchingBetweenInShopAndEditActivity();
        removeShoppingList(mNewListName);
        timeout(1000);
        removeShoppingList(mNewListName + "1");
    }

    private void switchingBetweenInShopAndEditActivity(){
        // Сначала проверим переключение в уже сохраненном списке
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Нажимаем два раза на кнопку "Increase"
        getBtnIncreaseViewInteraction(mNewProductNamePattern + "1")
                .perform(click())
                .perform(click());

        // Переключаемся на активность "В магазине"
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("3.0"))));

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
        getBtnIncreaseViewInteraction(mNewProductNamePattern + "1").perform(click());

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
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName + "1"));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверяем, что в активности "В магазине" в текстовом поле отображается число 2
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("2.0"))));

        // Переключаемся на активность "Редактирование списка"
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        pressBack();
        pressBack(); // вернулись к основной активности

        timeout(1000);
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
                "" + mNewProductNamePattern + "2, 1.0, " + mActivity.getString(R.string.default_unit) + ", 0.0;"
                        + "\n" + mNewProductNamePattern + "1, 1.0, " + mActivity.getString(R.string.default_unit) + ", 0.0;");
    }

    @Test
    @LargeTest
    public void shoppingListEditingActivity_LoadShoppingList(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(false);
        setSettingsShowPrices(false);
        loadShoppingList();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(true);
        setSettingsShowPrices(true);
        loadShoppingList();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(false);
        setSettingsShowPrices(true);
        loadShoppingList();
        removeShoppingList(mNewListName);
        setSettingsShowUnits(true);
        setSettingsShowPrices(false);
        loadShoppingList();
        removeShoppingList(mNewListName);
    }

    private void loadShoppingList(){
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

        pressBack();

        timeout(1000);
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

        pressBack();
        pressBack();
    }

    @Test
    @LargeTest
    public void shoppingListEditingActivity_UnitsTest_WithoutPrices(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(true);
        setSettingsShowPrices(false);
        unitsTest();
    }

    @Test
    @LargeTest
    public void shoppingListEditingActivity_UnitsTest_WithPrices(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(true);
        setSettingsShowPrices(true);
        unitsTest();
    }

    private void unitsTest(){
        // Создаем новый список покупок, добавляем в него три товара, и создаем несколько новых ед. измерения.
        addNewShoppingList();
        editNewShoppingList();
        addNewUnit(mNewUnitNamePattern + "1", mNewUnitShortNamePattern + "1");
        addNewUnit(mNewUnitNamePattern + "2", mNewUnitShortNamePattern + "2");
        addNewUnit(mNewUnitNamePattern + "3", mNewUnitShortNamePattern + "3");
        // Открываем его в активности редактирования.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        // Проверяем, что для всех позиций списка указана ед. измерения по умолчанию
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(R.string.default_unit)));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(R.string.default_unit)));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        // Переключаемся в режим В маназине, проверяем, что для всех позиций указана ед. измерения по умолчанию.
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        // Меняем ед. измерения для первой позиции
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "1").perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern + "1, " + mNewUnitShortNamePattern + "1"))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern + "1, " + mNewUnitShortNamePattern + "1")).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(mNewUnitShortNamePattern + "1")));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(R.string.default_unit)));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        // Переходим в режим В магазине и проверяем, что ед. изменилась только у первой позиции.
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        // Возвращаемся в режим редактировпния и меняем у второй позиции.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "2").perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern + "2, " + mNewUnitShortNamePattern + "2"))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern + "2, " + mNewUnitShortNamePattern + "2")).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(mNewUnitShortNamePattern + "1")));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(mNewUnitShortNamePattern + "2")));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        // В режиме В магазине проверяем, что ед. измерегия изменились у первой и второй позиции, но не изменилась у третьей.
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mNewUnitShortNamePattern + "2"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        // Открываем товар из первой и второй позиции и проверяем, что ед. измерения по умолчанию изменена.
        onView(recyclerViewItemWithText(mNewProductNamePattern + "1")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern + "1")));
        pressBack();
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern + "2")));
        pressBack();
        onView(recyclerViewItemWithText(mNewProductNamePattern + "3")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnUnit)).check(matches(withText(R.string.default_unit)));
        // Снова заходим в режим редактирования списка и меняем ед. измерения у второй позиции.
        pressBack();
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "2").perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern + "3, " + mNewUnitShortNamePattern + "3"))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern + "3, " + mNewUnitShortNamePattern + "3")).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(mNewUnitShortNamePattern + "1")));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(mNewUnitShortNamePattern + "3")));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        // Переходим в режим В магазине и проверяем
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mNewUnitShortNamePattern + "3"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        // Открываем товар из второй позиции и проверяем, что ед.измернния по умолчанию так же изменилась.
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern + "3")));
        pressBack();
        // Заходим в режим редактирования, удаляем вторую позицию, добавляем данный товар снова. Проверяем, что у вновь добавленного товаоа отобразилась последняя измененная ед. измерения.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getImgDeleteViewInteraction(mNewProductNamePattern + "2").perform(click());
        addProductInList(mNewProductNamePattern + "2", false);
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mNewUnitShortNamePattern + "3"))));
        // Создаем новый товар, указываем ед. измерения.
        pressBack();
        pressBack();
        addNewProduct(mNewProductNamePattern + "4");
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern + "4"))));
        onView(recyclerViewItemWithText(mNewProductNamePattern + "4")).perform(click());
        onView(withId(R.id.btnUnit)).check(matches(withText(R.string.default_unit)));
        onView(withId(R.id.btnUnit)).perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern + "1, " + mNewUnitShortNamePattern + "1"))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern + "1, " + mNewUnitShortNamePattern + "1")).perform(click());
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern + "1")));
        onView(withId(R.id.btnSave)).perform(click());
        pressBack();
        // Открываем список покупок и добавляем данный товар. Проверяем, что ед. измерения отобразилась выбранная. Так же проверяем в режиме В магазине.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        addProductInList(mNewProductNamePattern + "4", false);
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(mNewUnitShortNamePattern + "1")));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(mNewUnitShortNamePattern + "3")));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        getTvUnitViewInteraction(mNewProductNamePattern + "4").check(matches(withText(mNewUnitShortNamePattern + "1")));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mNewUnitShortNamePattern + "3"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        getTvCountViewInteraction(mNewProductNamePattern + "4").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        // В режиме редактирования меняем ед. измерения у нового товара. Проверяем изменение в режиме в Магазине и в карточке товара.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "4").perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern + "2, " + mNewUnitShortNamePattern + "2"))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern + "2, " + mNewUnitShortNamePattern + "2")).perform(click());
        getTvUnitViewInteraction(mNewProductNamePattern + "1").check(matches(withText(mNewUnitShortNamePattern + "1")));
        getTvUnitViewInteraction(mNewProductNamePattern + "2").check(matches(withText(mNewUnitShortNamePattern + "3")));
        getTvUnitViewInteraction(mNewProductNamePattern + "3").check(matches(withText(R.string.default_unit)));
        getTvUnitViewInteraction(mNewProductNamePattern + "4").check(matches(withText(mNewUnitShortNamePattern + "2")));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString(mNewUnitShortNamePattern + "1"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString(mNewUnitShortNamePattern + "3"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString(mActivity.getString(R.string.default_unit)))));
        getTvCountViewInteraction(mNewProductNamePattern + "4").check(matches(withText(containsString(mNewUnitShortNamePattern + "2"))));
        pressBack();
    }

    @Test
    @LargeTest
    public void shoppingListEditingActivity_PricesTest_WithoutUnits(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(false);
        setSettingsShowPrices(true);
        pricesTest();
    }

    @Test
    @LargeTest
    public void shoppingListEditingActivity_PricesTest_WithUnits(){
        // Проводим тест для различных значений настроек
        setSettingsShowUnits(true);
        setSettingsShowPrices(true);
        pricesTest();
    }

    private void pricesTest(){
        // Создаем новый список покупок.
        addNewShoppingList();
        editNewShoppingList();
        // Открываем в активностях редактировпния и В магазине и проверяем, что для всех позиций укащана цена 0.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        // Проверяем, что для всех позиций списка указана ед. измерения по умолчанию
        getEtPriceViewInteraction(mNewProductNamePattern + "1").check(matches(withText("0.00")));
        getEtPriceViewInteraction(mNewProductNamePattern + "2").check(matches(withText("0.00")));
        getEtPriceViewInteraction(mNewProductNamePattern + "3").check(matches(withText("0.00")));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("0.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("0.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        // В режиме редактировпния меняем цену у первой позиции (вводим в поле). Проверяем корректность изменения в активности В магазине. При этом у остальных должен остаться 0.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getEtPriceViewInteraction(mNewProductNamePattern + "1").perform(clearText()).perform(typeText("1.23"));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("0.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        // Меняем цену у второй позиции (кнопками плюс и минус). Проверяем В магазине.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getEtPriceViewInteraction(mNewProductNamePattern + "2").perform(clearText()).perform(typeText("4.0"));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("4.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        // Открываем товары первой, второй и третьей позиции. Проверяем, что последняя цена заполнена верно.
        onView(recyclerViewItemWithText(mNewProductNamePattern + "1")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.etLastPrice)).check(matches(withText("1.23")));
        pressBack();
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.etLastPrice)).check(matches(withText("4.00")));
        pressBack();
        onView(recyclerViewItemWithText(mNewProductNamePattern + "3")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.etLastPrice)).check(matches(withText("")));
        onView(withId(R.id.etLastPrice)).check(matches(withHint(mActivity.getString(R.string.enter_price))));
        pressBack();
        // В активносьи редактирования для второй позиции снова меняем цену. Проверяем В магазине и в карточке товара.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getEtPriceViewInteraction(mNewProductNamePattern + "2").perform(clearText()).perform(typeText("5.32"));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("5.32"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(longClick());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.etLastPrice)).check(matches(withText("5.32")));
        pressBack();
        // Удаляем вторую позицию из списка. Проверяем надпись итогов. Добавляем снова. Проверяем, что цена отображается последняя (в том числе и в активности В магазине). Проверяем надпись итогов.
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getImgDeleteViewInteraction(mNewProductNamePattern + "2").perform(click());
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "1.23", "1.23"))));
        addProductInList(mNewProductNamePattern + "2", false);
        getEtPriceViewInteraction(mNewProductNamePattern + "2").check(matches(withText("5.32")));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "6.55", "6.55"))));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("5.32"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "6.55", "6.55"))));
        // Создаем новый товар, указываем цену в карточке товара. Добавляем товар в список. Проверяем цену и итоговую запись в списках. Проверяем итоговую надпись списка на основном экране.
        pressBack();
        addNewProduct(mNewProductNamePattern + "4");
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern + "4"))));
        onView(recyclerViewItemWithText(mNewProductNamePattern + "4")).perform(click());
        onView(withId(R.id.etLastPrice)).check(matches(withText("")));
        onView(withId(R.id.etLastPrice)).check(matches(withHint(mActivity.getString(R.string.enter_price))));
        onView(withId(R.id.etLastPrice)).perform(clearText()).perform(typeText("250.25"));
        onView(withId(R.id.btnSave)).perform(click());
        pressBack();
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        addProductInList(mNewProductNamePattern + "4", false);
        getEtPriceViewInteraction(mNewProductNamePattern + "4").check(matches(withText("250.25")));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "256.80"))));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("5.32"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "4").check(matches(withText(containsString("250.25"))));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "256.80"))));
        pressBack();
        getRecyclerViewItemTvSumInfoViewInteraction(mNewListName).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "256.80"))));
        // В активности В магазине вычеркиваем товар, проверяем итоговую надпись. Вычеркиваем еще один и проверяем итоговую надпись (в том числе и в активности редактирования).
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(swipeLeft());
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "251.48"))));
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        getEtPriceViewInteraction(mNewProductNamePattern + "1").check(matches(withText("1.23")));
        getEtPriceViewInteraction(mNewProductNamePattern + "2").check(matches(withText("5.32")));
        getEtPriceViewInteraction(mNewProductNamePattern + "3").check(matches(withText("0.00")));
        getEtPriceViewInteraction(mNewProductNamePattern + "4").check(matches(withText("250.25")));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "251.48"))));
        // Проверяем итоговую надпись списка на основном экране.
        pressBack();
        pressBack();
        getRecyclerViewItemTvSumInfoViewInteraction(mNewListName).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "256.80", "251.48"))));
        // Добавить количество. Проверить итоговую надпись в активностях.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        getBtnIncreaseViewInteraction(mNewProductNamePattern + "2")
                .perform(click())
                .perform(click())
                .perform(click())
                .perform(click());
        getBtnDecreaseViewInteraction(mNewProductNamePattern + "2")
                .perform(click());
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "272.76", "251.48"))));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        getTvCountViewInteraction(mNewProductNamePattern + "1").check(matches(withText(containsString("1.23"))));
        getTvCountViewInteraction(mNewProductNamePattern + "2").check(matches(withText(containsString("5.32"))));
        getTvCountViewInteraction(mNewProductNamePattern + "3").check(matches(withText(containsString("0.0"))));
        getTvCountViewInteraction(mNewProductNamePattern + "4").check(matches(withText(containsString("250.25"))));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "272.76", "251.48"))));
        pressBack();
        getRecyclerViewItemTvSumInfoViewInteraction(mNewListName).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "272.76", "251.48"))));
        // Переходим в акивность В магазине, удаляем вычеркнутые и проверяем изменение итоговой записи.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_remove_checked))).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 4)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSumInfo)).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "251.48", "251.48"))));
        pressBack();
        getRecyclerViewItemTvSumInfoViewInteraction(mNewListName).check(matches(withText(mActivity.getString(R.string.shopping_list_info, "251.48", "251.48"))));
    }

    @Test
    @MediumTest
    public void shoppingListEditingActivity_ChangeProductNameTest(){
        addNewShoppingList();
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        // Открываем карточку товара
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).perform(longClick());
        // Изменяем наименование
        onView(withId(R.id.etProductName)).perform(clearText());
        onView(withId(R.id.etProductName)).perform(typeText(mNewProductNamePattern + "5"));
        onView(withId(R.id.btnSave)).perform(click());
        // Возвращаемся в активность редактирования, и проверяем, что наименование поменялось
        onView(recyclerViewItemWithText(mNewProductNamePattern + "5")).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + "2")).check(doesNotExist());
    }
}
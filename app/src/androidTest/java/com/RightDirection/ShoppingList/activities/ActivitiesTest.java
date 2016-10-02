package com.RightDirection.ShoppingList.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ActivitiesTest {

    private static String mNewListName = "newTestShoppingList";
    private final static String mNewProductNamePattern = "testNewProduct";
    private static UiDevice mDevice = null;
    private static MainActivity mActivity = null;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    /**
     * Процедура необходима для поиска объектов класса ListItem в ListAdapter по имени
     */
    private static Matcher<Object> withItemValue(final String value) {
        return new BoundedMatcher<Object, ListItem>(ListItem.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has value " + value);
            }

            @Override
            public boolean matchesSafely(ListItem item) {
                return item.getName().equals(String.valueOf(value));
            }
        };
    }

    /**
     * Процедура необходима для поиска объектов класса ListItem в ListAdapter по части имени
     */
    private static Matcher<Object> withItemValueContains(final String value) {
        return new BoundedMatcher<Object, ListItem>(ListItem.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("contains value " + value);
            }

            @Override
            public boolean matchesSafely(ListItem item) {
                return item.getName().contains(String.valueOf(value));
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @After
    public void tearDown() throws Exception {
        ContentResolver contentResolver = mActivity.getContentResolver();

        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME +  " = '" + mNewListName + "'", null);
        contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME +  " LIKE '%" + mNewProductNamePattern + "%'", null);
    }

    @Test
    @MediumTest
    public void testAddAndEditNewShoppingList(){
        addNewShoppingList();
        editNewShoppingList();
    }

    private void addNewShoppingList(){
        // Нажмем на кнопку добавления нового списка покупок
        onView(withId(R.id.fabAddNewShoppingList)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Плюс"
        String textForTyping = mNewProductNamePattern + "1";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        // Проверим, что элемент появился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Готово"
        textForTyping = mNewProductNamePattern + "2";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping), pressImeActionButton());
        // Проверим, что элемент добавился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // "Глюк" Espresso - если не закрыть клавиатуру перед вызововм диалгового окна, то
        // Espresso в большинстве случаев не открывает клавиатуру при печати в текстовом поле
        // inputNewListName диалогового окна, однако пытается произвести действия с диалоговым
        // окном в том месте, как будто оно было смещено (видимо, действия производятся
        // по координатам экрана). Из-за этого вместо печати текста в поле диалогового окна,
        // например, может открыться Activity редактирования товара и текст начнет набираться
        // в текстовом поле этой Activity. Принудительное закрытие клавиатуры перед нажатием
        // кнопки сохранения списка решает эту проблему.
        onView(withId(R.id.newItemEditText)).perform(closeSoftKeyboard());

        // Сохраним список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Введем имя нового списка
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверим, что осуществлен переход к активности MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверим, что сохраненный список покупок отобразился в списке в MainActivity
        onData(withItemValue(mNewListName)).check(matches(isDisplayed()));
    }

    private void editNewShoppingList() {
        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.imgEdit)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // Добавляем третий элемент в список покупок (нажатием на кнопку "Плюс")
        String textForTyping = mNewProductNamePattern + "3";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        // Проверим, что элемент появился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testShoppingListEditingActivity_ProductsCountEnter() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.imgEdit)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // На одном из элементов проверяем, что количество = 1
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .check(matches(withText("1.0")));

        // Нажимаем два раза на кнопку "Increase"
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.imgIncrease))
                .perform(click())
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .check(matches(withText("3.0")));

        // Нажимаем кнопку "Decrease"
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.imgDecrease))
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 2
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .check(matches(withText("2.0")));

        // Вводим в текстовое поле количество 5
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .perform(clearText())
                .perform(typeText("5.0"));

        // Попытаемся ввести не число
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .perform(typeText("Not number"))
        // Проверяем, что в окне снова отображается 5
                .check(matches(withText("5.0")));

        // Выбираем другой элемент списка. С помощью кнопки добавим количество до 11
        for (int i=0; i<10; i++) {
            onData(withItemValue(mNewProductNamePattern + "2")).onChildView(withId(R.id.imgIncrease))
                    .perform(click());
        }

        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
                .check(matches(withText("5.0")));
        onData(withItemValue(mNewProductNamePattern + "2")).onChildView(withId(R.id.etCount))
                .check(matches(withText("11.0")));

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверяем, что в таблице содержимого списка покупок для редактированных элементов проставлено корректное количество
        ContentResolver contentResolver = mActivity.getContentResolver();
        Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, ShoppingListContentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "1' OR "
                + ShoppingListContentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "2'",
                null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 2);
        int keyCountIndex = cursor.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_COUNT);
        int keyNameIndex = cursor.getColumnIndexOrThrow(ShoppingListContentProvider.KEY_NAME);
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
    }

    private void testShoppingListInShopActivity_CountAppearing() {
        // Клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(click());

        // Проверяем, что открылась активность "В магазине"
        onView(withId(R.id.action_filter)).check(matches(isDisplayed()));

        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.txtCount))
                .check(matches(withText("5.0")));
        onData(withItemValue(mNewProductNamePattern + "2")).onChildView(withId(R.id.txtCount))
                .check(matches(withText("11.0")));
    }

    @Test
    @MediumTest
    public void testRenameShoppingList() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку переименования списка
        onView(withId(R.id.imgChangeListName)).perform(click());

        // Вводим новое имя списка покупок
        mNewListName += "Changed";
        onView(withId(R.id.inputNewListName)).perform(clearText());
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверяем, что в списке отображен переименованный элемент
        onData(withItemValue(mNewListName)).check(matches(isDisplayed()));
    }

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
        onData(withItemValue(mNewListName)).perform(click());

        // Пробуем вычеркнуть товары обычным нажатие. Проверяем, что окно "Победа!" не отобразилось
        for (int i = 1; i <= 3; i++){
            onData(withItemValue(mNewProductNamePattern + i)).perform(click());
        }
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(doesNotExist());

        // Вычеркиванием все товары
        for (int i = 1; i <= 3; i++){
            onData(withItemValue(mNewProductNamePattern + i)).perform(swipeRight());
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
        onData(withItemValue(mNewListName)).perform(click());

        // Вычеркиванием все товары
        for (int i = 1; i <= 3; i++){
            onData(withItemValue(mNewProductNamePattern + i)).perform(click());
        }
        // Проверяем, что появилось окно с надписью "Победа!"
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));

        // Возвращаемся к основной активности
        pressBack();
        pressBack();
    }

    private void openSettings() {
        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_settings))).perform(click());

        // Проверяем, что открылась форма настроек
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testSendReceiveEmail() throws UiObjectNotFoundException {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку отправки списка по почте
        onView(withId(R.id.imgSendListByEmail)).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text("Shopping list '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        UiObject emailAttachments = mDevice.findObject(new UiSelector().text("Shopping list '" + mNewListName + "'.json"));
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
        onData(withItemValueContains(mActivity.getString(R.string.loaded))).check(matches(isDisplayed()));
    }

    private void timeout(int duration) {
        long startDate = new Date().getTime();
        long now = new Date().getTime();
        while ((now - startDate) < duration){
            now = new Date().getTime();
        }
    }

    @Test
    @MediumTest
    public void testDeleteShoppingList() {
        addNewShoppingList();

        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());
        // В меню действий нажимаем кнопку удаления списка
        onView(withId(R.id.imgDelete)).perform(click());
        // Проверяем, что открылось окно с вопросом об удалении списка
        onView(withText(mActivity.getString(R.string.delete_shopping_list_question))).check(matches(isDisplayed()));
        // Отклоняем удаление
        onView(withText(mActivity.getString(R.string.cancel))).perform(click());
        // Проверяем, что список покупок не исчез
        onData(withItemValue(mNewListName)).check(matches(isDisplayed()));

        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());
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

    private void checkDataNotExistInList(String text) {
        ShoppingListFragment shoppingListFragment = (ShoppingListFragment)mActivity.getFragmentManager()
                .findFragmentById(R.id.frgShoppingLists);
        assertNotNull(shoppingListFragment);
        ListAdapter listAdapter = (ListAdapter) shoppingListFragment.getListAdapter();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            ListItem listItem = (ListItem)listAdapter.getItem(i);
            assertThat("Item is in the list", text, is(not(listItem.getName())));
        }
    }

    @Test
    @MediumTest
    public void testOpenSettings() {
        openSettings();
    }

    @Test
    @MediumTest
    public void testProducts() {
        // Нажимаем кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());

        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));

        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());

        // Вводим название нового продукта и нажимаем кнопку сохранения
        String textForTyping = mNewProductNamePattern + "testProducts";
        onView(withId(R.id.etProductName)).perform(typeText(textForTyping));
        onView(withId(R.id.btnSaveProduct)).perform(click());

        // Проверяем, что новый продукт отобразился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }
}
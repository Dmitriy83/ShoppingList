package com.RightDirection.ShoppingList.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapter;
import com.RightDirection.ShoppingList.items.ListItem;
import com.RightDirection.ShoppingList.utils.contentProvider;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
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
import static android.support.test.espresso.intent.Checks.checkArgument;
import static android.support.test.espresso.intent.Checks.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ActivitiesTest {

    private static String mNewListName = "newTestShoppingList";
    private final static String mNewProductNamePattern = "testNewProduct";
    private final static String mNewCategoryNamePattern = "testNewCategory";
    private static UiDevice mDevice = null;
    private static MainActivity mActivity = null;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Сначала выключим появление подсказок
        switchOffHelpActivities();
    }

    @After
    public void tearDown() throws Exception {
        ContentResolver contentResolver = mActivity.getContentResolver();

        contentResolver.delete(contentProvider.SHOPPING_LISTS_CONTENT_URI,
                contentProvider.KEY_NAME +  " LIKE '%Test%'", null);
        contentResolver.delete(contentProvider.PRODUCTS_CONTENT_URI,
                contentProvider.KEY_NAME +  " LIKE '%test%'", null);
        contentResolver.delete(contentProvider.CATEGORIES_CONTENT_URI,
                contentProvider.KEY_CATEGORY_NAME +  " LIKE '%test%'", null);
    }

    private void switchOffHelpActivities(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean showHelpScreens = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_show_help_screens), true);
        if (showHelpScreens) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_key_show_help_screens))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    private static Matcher<View> isChildOfRecyclerViewItem(final Matcher<View> recyclerViewItem) {
        checkNotNull(recyclerViewItem);
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is child of recycler view item: ");
                recyclerViewItem.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent productRepresent = view.getParent();
                if (((ViewGroup) productRepresent).getId() != R.id.productRepresent){
                    // Еще не тот контейнер. Повторим получение родителя
                    productRepresent = productRepresent.getParent();
                }
                View txtName = ((ViewGroup) productRepresent).findViewById(R.id.txtName);
                return recyclerViewItem.matches(txtName);
            }
        };
    }

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

    private Matcher<View> recyclerViewItemWithText(final String itemText)
    {
        checkArgument(!TextUtils.isEmpty(itemText),"cannot be null");
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        withText(itemText)).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView with text: " + itemText);
            }
        };
    }

    private Matcher<View> recyclerViewItemContainsText(final String subString)
    {
        checkArgument(!TextUtils.isEmpty(subString),"cannot be null");
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        containsText(subString)).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView that contains text: " + subString);
            }
        };
    }

    private static Matcher<View> containsText(final String subString) {
        checkArgument(!TextUtils.isEmpty(subString),"cannot be null");
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("contains text: " + subString);
            }

            @Override
            public boolean matchesSafely(TextView textView) {
                return textView.getText().toString().contains(String.valueOf(subString));
            }
        };
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
        //onData(withItemValue(textForTyping)).check(matches(isDisplayed()));
        //onView(withId(R.id.rvProducts)).check(matches(atPosition(0, hasDescendant(withText(textForTyping)))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Готово"
        textForTyping = mNewProductNamePattern + "2";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping), pressImeActionButton());
        // Проверим, что элемент добавился в списке
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

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
        //onView(withId(R.id.rvShoppingLists)).check(matches(atPosition(2, hasDescendant(withText(mNewListName)))));
        onView(recyclerViewItemWithText(mNewListName)).check(matches(isDisplayed()));
    }

    private void editNewShoppingList() {
        // Длинный клик на новом списке покупок
        //onData(withItemValue(mNewListName)).perform(longClick());
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // Добавляем третий элемент в список покупок (нажатием на кнопку "Плюс")
        String textForTyping = mNewProductNamePattern + "3";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        // Проверим, что элемент появился в списке
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

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
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // На одном из элементов проверяем, что количество = 1
        //onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.etCount))
        //        .check(matches(withText("1.0")));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("1.0")));


        // Нажимаем два раза на кнопку "Increase"
        //onData(withItemValue(mNewProductNamePattern + "1")).onChildView(withId(R.id.imgIncrease))
        onView(allOf(withId(R.id.imgIncrease),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .perform(click())
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("3.0")));

        // Нажимаем кнопку "Decrease"
        onView(allOf(withId(R.id.imgDecrease),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .perform(click());

        // Проверяем, что в текстовом поле отображается число 2
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("2.0")));

        // Вводим в текстовое поле количество 5
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .perform(clearText())
                .perform(typeText("5.0"));

        // Попытаемся ввести не число
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .perform(typeText("Not number"))
        // Проверяем, что в окне снова отображается 5
                .check(matches(withText("5.0")));

        // Выбираем другой элемент списка. С помощью кнопки добавим количество до 11
        for (int i=0; i<10; i++) {
            onView(allOf(withId(R.id.imgIncrease),
                    isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "2"))))
                    .perform(click());
        }

        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("5.0")));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "2"))))
                .check(matches(withText("11.0")));

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверяем, что в таблице содержимого списка покупок для редактированных элементов проставлено корректное количество
        ContentResolver contentResolver = mActivity.getContentResolver();
        Cursor cursor = contentResolver.query(contentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                null, contentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "1' OR "
                + contentProvider.KEY_NAME + " = '" + mNewProductNamePattern + "2'",
                null, null);
        assertNotNull(cursor);
        assertTrue(cursor.getCount() == 2);
        int keyCountIndex = cursor.getColumnIndexOrThrow(contentProvider.KEY_COUNT);
        int keyNameIndex = cursor.getColumnIndexOrThrow(contentProvider.KEY_NAME);
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
        onView(recyclerViewItemWithText(mNewListName)).perform(click());

        // Проверяем, что открылась активность "В магазине"
        onView(withId(R.id.action_filter)).check(matches(isDisplayed()));


        // Проверяем, что у одного элемента списка указано количество 5, у другого - 11
        onView(allOf(withId(R.id.txtCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("5.0")));
        onView(allOf(withId(R.id.txtCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "2"))))
                .check(matches(withText("11.0")));

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

    private void openSettings() {
        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_settings))).perform(click());

        // Проверяем, что открылась форма настроек
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).check(matches(isDisplayed()));
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

    private void checkDataNotExistInList(String text) {
        RecyclerView rv = (RecyclerView)mActivity.findViewById(R.id.rvShoppingLists);
        assertNotNull(rv);
        ListAdapter listAdapter = (ListAdapter) rv.getAdapter();
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            ListItem listItem = listAdapter.getItem(i);
            assertThat("Item is in the list", text, is(not(listItem.getName())));
        }
    }

    @Test
    @MediumTest
    public void testOpenSettings() {
        openSettings();
    }

    private void addNewProduct(String name){
        // Нажимаем кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());

        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));

        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());

        // Вводим название нового продукта и нажимаем кнопку сохранения
        onView(withId(R.id.etProductName)).perform(typeText(name));
        onView(withId(R.id.btnSaveProduct)).perform(click());
    }

    @Test
    @LargeTest
    public void testProducts() {
        String textForTyping = mNewProductNamePattern;
        addNewProduct(textForTyping);

        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

        // Проверим удаление продукта из списка продуктов
        onView(allOf(withId(R.id.imgDelete),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(textForTyping))))
                .perform(click());
        // Товар более не должен отображаться в списке
        onView(recyclerViewItemWithText(textForTyping)).check(doesNotExist());

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверим редактирование продукта из активности редактирования списка товаров
        addNewShoppingList();
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + "1")).perform(longClick());
        textForTyping = mNewProductNamePattern + "testProducts";
        onView(withId(R.id.etProductName)).perform(clearText());
        onView(withId(R.id.etProductName)).perform(typeText(textForTyping));
        onView(withId(R.id.btnSaveProduct)).perform(click());
        // Проверяем, что вернулись к активности редактирования списка товаров
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
        //  Товар в списке изменил наименование:
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Протестируем выбор категорий
        addNewCategory();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
        // Нажимаем кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());
        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));
        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());
        textForTyping = mNewProductNamePattern + "testProducts2";
        onView(withId(R.id.etProductName)).perform(typeText(textForTyping));
        // Скроем клавиатуру
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        // Проверяем, что в поле Категория написано выражение по умолчанию
        onView(withId(R.id.btnChooseCategory))
                .check(matches(withText("")));
        onView(withId(R.id.btnChooseCategory)).perform(click());
        // Выбираем созданную категорию
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewCategoryNamePattern))));
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Проверяем, что категория отобразилась в активности
        onView(withId(R.id.btnChooseCategory)).check(matches(withText(mNewCategoryNamePattern
                + mActivity.getString(R.string.three_dots))));
        onView(withId(R.id.btnSaveProduct)).perform(click());
        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));
        // Еще раз зайдем в продукт и убедимся, что категория сохранилась
        onView(recyclerViewItemWithText(textForTyping)).perform(click());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnChooseCategory)).check(matches(withText(mNewCategoryNamePattern
                + mActivity.getString(R.string.three_dots))));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
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
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .perform(click())
                .perform(click());

        // Переключаемся на активность "В магазине"
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());

        // Проверяем, что в текстовом поле отображается число 3
        onView(allOf(withId(R.id.txtCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
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
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
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
                isChildOfRecyclerViewItem(recyclerViewItemWithText(mNewProductNamePattern + "1"))))
                .check(matches(withText("2.0")));

        // Переключаемся на активность "Редактирование списка"
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));
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
    public void inShopActivity_SendEmail() throws UiObjectNotFoundException{
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
    public void shoppingListEditingActivity_SendEmail() throws UiObjectNotFoundException{
        addNewShoppingList();

        // Переходим в активность "Редактирование списка"
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Нажимаем кнопку отправки списка покупок по почте
        onView(withText(mActivity.getString(R.string.send_by_email))).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        // Проверяем, что в теле письма правильно представлен список
        UiObject emailBody = mDevice.findObject(new UiSelector().text("" + mNewProductNamePattern + "2, 1.0;" + "\n" + mNewProductNamePattern + "1, 1.0;"));
        assertTrue(emailBody.exists());
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

    private void loadAndCheckList(){
        // В текстовое поле вставляем текст для загрузки
        onView(withId(R.id.etTextForLoading)).perform(typeText("test1, 5; test2, 3; test3; test4; test5 555, 2.3"));

        // Нажимаем кнопку загрузить
        onView(withId(R.id.btnLoad)).perform(click());

        // Скроем клавиатуру
        onView(withId(R.id.newItemFragment)).perform(closeSoftKeyboard());

        // Проверим загружены ли элементы
        onView(recyclerViewItemWithText("test1")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText("test1"))))
                .check(matches(withText("5.0")));
        onView(recyclerViewItemWithText("test2")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText("test2"))))
                .check(matches(withText("3.0")));
        onView(recyclerViewItemWithText("test3")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText("test3"))))
                .check(matches(withText("1.0")));
        onView(recyclerViewItemWithText("test4")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText("test4"))))
                .check(matches(withText("1.0")));
        onView(recyclerViewItemWithText("test5 555")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                isChildOfRecyclerViewItem(recyclerViewItemWithText("test5 555"))))
                .check(matches(withText("2.3")));

        onView(recyclerViewItemWithText("testNewProduct1")).check(doesNotExist());
        onView(recyclerViewItemWithText("testNewProduct2")).check(doesNotExist());
    }

    @Test
    @MediumTest
    public void testCategories(){
        addNewCategory();

        // Проверим редактирование категории
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        String textForTyping = mNewProductNamePattern + "Edited";
        onView(withId(R.id.etCategoryName)).perform(clearText());
        onView(withId(R.id.etCategoryName)).perform(typeText(textForTyping));
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что отредактированный продукт отобразился в списке
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));

        // Проверим удаление категории из списка
        onView(allOf(withId(R.id.imgDelete),
                isChildOfRecyclerViewItem(recyclerViewItemWithText(textForTyping))))
                .perform(click());
        // Товар более не должен отображаться в списке
        onView(recyclerViewItemWithText(textForTyping)).check(doesNotExist());

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    private void addNewCategory(){
        // Нажимаем кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Категории"
        onView(withText(mActivity.getString(R.string.action_edit_categories_list))).perform(click());

        // Проверяем, что открылась активность "Категории"
        onView(withText(mActivity.getString(R.string.action_edit_categories_list))).check(matches(isDisplayed()));

        // Нажимаем кнопку добавления новой категории
        onView(withId(R.id.fabAddCategory)).perform(click());

        // Вводим название нового продукта и нажимаем кнопку сохранения
        onView(withId(R.id.etCategoryName)).perform(typeText(mNewCategoryNamePattern));
        onView(withId(R.id.btnSave)).perform(click());

        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewCategoryNamePattern))));
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).check(matches(isDisplayed()));
    }
}
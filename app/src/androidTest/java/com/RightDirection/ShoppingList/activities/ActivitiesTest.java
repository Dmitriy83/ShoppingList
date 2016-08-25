package com.RightDirection.ShoppingList.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.MediumTest;

import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;
import com.RightDirection.ShoppingList.views.ShoppingListFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ContentResolver contentResolver = mActivity.getContentResolver();

        contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME +  " = '" + mNewListName + "'", null);
        contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                ShoppingListContentProvider.KEY_NAME +  " LIKE '%" + mNewProductNamePattern + "%'", null);
    }

    @Test
    @MediumTest
    public void testAddNewShoppingList(){
        // Нажмем на кнопку добавления нового списка покупок
        onView(withId(R.id.fabAddNewShoppingList)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.btnShoppingListSave)).check(matches(isDisplayed()));

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
        onView(withId(R.id.btnShoppingListSave)).perform(click());

        // Введем имя нового списка
        onView(withId(R.id.inputNewListName)).perform(typeText(mNewListName));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Проверим, что осуществлен переход к активности MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));

        // Проверим, что сохраненный список покупок отобразился в списке в MainActivity
        onData(withItemValue(mNewListName)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testOpenShoppingListForEditing() {
        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.imgEdit)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.btnShoppingListSave)).check(matches(isDisplayed()));

        // Добавляем третий элемент в список покупок (нажатием на кнопку "Плюс")
        String textForTyping = mNewProductNamePattern + "3";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        // Проверим, что элемент появился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // Сохраняем список покупок
        onView(withId(R.id.btnShoppingListSave)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testRenameShoppingList() {
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
    public void testSendByEmail() {
        // Длинный клик на новом списке покупок
        onData(withItemValue(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку отправки списка по почте
        onView(withId(R.id.imgSendListByEmail)).perform(click());

        // С помощбю UIAutomator ищем проверяем сфорировалось ли письмо?
        UiObject emailSubject = mDevice.findObject(new UiSelector().text("Shopping list '" + mNewListName + "'"));
        assertTrue(emailSubject.exists());
        UiObject emailAttachments = mDevice.findObject(new UiSelector().text("Shopping list '" + mNewListName + "'.json"));
        assertTrue(emailAttachments.exists());
    }

    @Test
    @MediumTest
    public void testDeleteShoppingList() {
        // Нажмем на кнопку добавления нового списка покупок
        onView(withId(R.id.fabAddNewShoppingList)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.btnShoppingListSave)).check(matches(isDisplayed()));

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Плюс"
        String textForTyping = mNewProductNamePattern + "1";
        onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
        onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        // Проверим, что элемент появился в списке
        onData(withItemValue(textForTyping)).check(matches(isDisplayed()));

        // См. описание в процедуре testAddNewShoppingList
        onView(withId(R.id.newItemEditText)).perform(closeSoftKeyboard());

        // Сохраним список покупок
        onView(withId(R.id.btnShoppingListSave)).perform(click());

        // Введем имя нового списка
        String newListNameForDeleting = mNewListName + "(for deleting)";
        onView(withId(R.id.inputNewListName)).perform(typeText(newListNameForDeleting));
        onView(withText(mActivity.getString(R.string.ok))).perform(click());

        // Длинный клик на новом списке покупок
        onData(withItemValue(newListNameForDeleting)).perform(longClick());
        // В меню действий нажимаем кнопку удаления списка
        onView(withId(R.id.imgDelete)).perform(click());
        // Проверяем, что открылось окно с вопросом об удалении списка
        onView(withText(mActivity.getString(R.string.delete_shopping_list_question))).check(matches(isDisplayed()));
        // Отклоняем удаление
        onView(withText(mActivity.getString(R.string.cancel))).perform(click());
        // Проверяем, что список покупок не исчез
        onData(withItemValue(newListNameForDeleting)).check(matches(isDisplayed()));

        // Длинный клик на новом списке покупок
        onData(withItemValue(newListNameForDeleting)).perform(longClick());
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
        checkDataNotExistInList(newListNameForDeleting);
    }

    private void checkDataNotExistInList(String text) {
        ShoppingListFragment shoppingListFragment = (ShoppingListFragment)mActivity.getFragmentManager()
                .findFragmentById(R.id.frgShoppingLists);
        assertNotNull(shoppingListFragment);
        ListAdapter listAdapter = (ListAdapter) shoppingListFragment.getListAdapter();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            assertThat("Item is in the list", text, is(not(listAdapter.getItem(i).getName())));
        }
    }

    @Test
    @MediumTest
    public void testOpenSettings() {
        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_settings))).perform(click());

        // Проверяем, что открылась форма настроек
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).check(matches(isDisplayed()));
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

    @Ignore("Пока не готов")
    @Test
    @MediumTest
    public void testReceiveShoppingListByEmail() {

    }
}
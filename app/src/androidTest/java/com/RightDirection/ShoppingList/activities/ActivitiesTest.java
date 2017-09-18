package com.RightDirection.ShoppingList.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.BaseListAdapter;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
abstract class ActivitiesTest {

    static String mNewListName = "newTestShoppingList'"; // Апостров добавлен, т.к. у пользователей возникала ошибка при добавлении этого символа к наименованию продуктов
    final static String mNewProductNamePattern = "testNewProduct'";
    final static String mNewCategoryNamePattern = "testNewCategory'";
    final static String mNewUnitNamePattern = "testNewUnit'";
    final static String mNewUnitShortNamePattern = "u.'";
    final static String mNewUnitNamePlusShortNamePattern = "testNewUnit', u.'";
    static UiDevice mDevice = null;
    MainActivity mActivity = null;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Сначала выключим появление подсказок
        switchOffHelpActivities();
        // Включим отображение картинок
        switchOnImages();
    }

    @After
    public void tearDown() throws Exception {
        ContentResolver contentResolver = mActivity.getContentResolver();

        contentResolver.delete(SL_ContentProvider.SHOPPING_LISTS_CONTENT_URI,
                SL_ContentProvider.KEY_NAME +  " LIKE '%Test%'", null);
        contentResolver.delete(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                SL_ContentProvider.KEY_NAME +  " LIKE '%test%'", null);
        contentResolver.delete(SL_ContentProvider.CATEGORIES_CONTENT_URI,
                SL_ContentProvider.KEY_CATEGORY_NAME +  " LIKE '%test%'", null);
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

    private void switchOnImages(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean showImages = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_show_images), true);
        if (!showImages) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_show_images))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    void openSettings() {
        openMainMenu();

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_settings))).perform(click());

        // Проверяем, что открылась форма настроек
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).check(matches(isDisplayed()));
    }

    void openMainMenu() {
        /*// Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());*/

        // Открываем панель навигации

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
    }

    void addNewShoppingList(){
        // Нажмем на кнопку добавления нового списка покупок
        onView(withId(R.id.fabAddNewShoppingList)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Плюс"
        addProductInList(mNewProductNamePattern + "1", false);

        // Добавим новый элемент в список товаров и базу данных нажатием на кнопку "Готово"
        addProductInList(mNewProductNamePattern + "2", true);

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

    void editNewShoppingList() {
        // Длинный клик на новом списке покупок
        //onData(withItemValue(mNewListName)).perform(longClick());
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Проверяем, что открылась активность редактирования списка покупок
        onView(withId(R.id.action_save_list)).check(matches(isDisplayed()));

        // Добавляем третий элемент в список покупок (нажатием на кнопку "Плюс")
        addProductInList(mNewProductNamePattern + "3", false);

        // Сохраняем список покупок
        onView(withId(R.id.action_save_list)).perform(click());

        // Проверяем, что снова открылась активность MainActivity
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    void addProductInList(String textForTyping, boolean pressImeActionButton){
        if (pressImeActionButton){
            onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping), pressImeActionButton());
        }else{
            onView(withId(R.id.newItemEditText)).perform(typeText(textForTyping));
            onView(withId(R.id.btnAddProductToShoppingList)).perform(click());
        }
        // Проверим, что элемент появился в списке
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));
    }

    void addProductInList(String textForTyping, @SuppressWarnings("SameParameterValue") boolean pressImeActionButton, @SuppressWarnings("SameParameterValue") double count){
        addProductInList(textForTyping, pressImeActionButton);

        // Изменим количество
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText(textForTyping)))))
                .perform(clearText())
                .perform(typeText(String.valueOf(count)));
    }

    void timeout(int duration) {
        long startDate = new Date().getTime();
        long now = new Date().getTime();
        while ((now - startDate) < duration){
            now = new Date().getTime();
        }
    }

    void checkDataNotExistInList(String text) {
        RecyclerView rv = (RecyclerView)mActivity.findViewById(R.id.rvShoppingLists);
        assertNotNull(rv);
        BaseListAdapter listAdapter = (BaseListAdapter) rv.getAdapter();
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            IListItem listItem = listAdapter.getItem(i);
            assertThat("Item is in the list", text, is(not(listItem.getName())));
        }
    }

    void addNewProduct(String name){
        // Нажимаем кнопку вызова подменю
        openMainMenu();

        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());

        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));

        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());

        // Вводим название нового продукта и нажимаем кнопку сохранения
        onView(withId(R.id.etProductName)).perform(typeText(name));
        onView(withId(R.id.btnSave)).perform(click());
    }

    void loadAndCheckList(){
        // В текстовое поле вставляем текст для загрузки
        onView(withId(R.id.etTextForLoading)).perform(typeText("test1, 5; test2, 3; test3; test4; test5 555, 2.3"));

        // Нажимаем кнопку загрузить
        onView(withId(R.id.btnLoad)).perform(click());

        // Скроем клавиатуру
        onView(withId(R.id.newItemFragment)).perform(closeSoftKeyboard());

        // Проверим загружены ли элементы
        onView(recyclerViewItemWithText("test1")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText("test1")))))
                .check(matches(withText("5.0")));
        onView(recyclerViewItemWithText("test2")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText("test2")))))
                .check(matches(withText("3.0")));
        onView(recyclerViewItemWithText("test3")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText("test3")))))
                .check(matches(withText("1.0")));
        onView(recyclerViewItemWithText("test4")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText("test4")))))
                .check(matches(withText("1.0")));
        onView(recyclerViewItemWithText("test5 555")).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.etCount),
                withParent(hasSibling(recyclerViewItemWithText("test5 555")))))
                .check(matches(withText("2.3")));

        onView(recyclerViewItemWithText("testNewProduct1")).check(doesNotExist());
        onView(recyclerViewItemWithText("testNewProduct2")).check(doesNotExist());

        pressBack();
    }

    void addNewCategory(){
        // Нажимаем кнопку вызова подменю
        openMainMenu();

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

    void setSettingsShowUnits(boolean show){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean showUnits = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_show_units), false);
        if (show != showUnits) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_show_units))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    void addNewUnit(){
        setSettingsShowUnits(true);

        // Нажимаем кнопку вызова подменю
        openMainMenu();

        // Выбираем "Ед. измерения"
        onView(withText(mActivity.getString(R.string.action_edit_units_list))).perform(click());

        // Нажимаем кнопку добавления новой категории
        onView(withId(R.id.fabAddUnit)).perform(click());

        // Вводим название новой ед. измерения, и нажимаем кнопку сохранения
        onView(withId(R.id.etName)).perform(typeText(mNewUnitNamePattern));
        onView(withId(R.id.etShortName)).perform(typeText(mNewUnitShortNamePattern));
        onView(withId(R.id.btnSave)).perform(click());

        // Проверяем, что новая ед. измерения отобразилась в списке
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern)).check(matches(isDisplayed()));
    }

    class RecyclerViewItemCountAssertion implements ViewAssertion {

        private final Matcher<Integer> matcher;

        @SuppressWarnings("unused")
        public RecyclerViewItemCountAssertion(int expectedCount) {
            this.matcher = is(expectedCount);
        }

        RecyclerViewItemCountAssertion(Matcher<Integer> matcher) {
            this.matcher = matcher;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), matcher);
        }

    }

    void checkEmailAppearing(String subject, String emailBodyText) throws UiObjectNotFoundException {
        UiObject btnEmail = mDevice.findObject(new UiSelector().text("Email"));
        if (btnEmail.exists()) {
            btnEmail.click();
            // Скроем клавиатуру
            mDevice.pressBack();
            UiObject emailSubject = mDevice.findObject(new UiSelector().text(subject));
            assertTrue(emailSubject.exists());
            // Проверяем, что в теле письма правильно представлен список
            UiObject emailBody = mDevice.findObject(new UiSelector().text(emailBodyText));
            assertTrue(emailBody.exists());
            //UiObject btnSend = mDevice.findObject(new UiSelector().description(mActivity.getString(R.string.send)));
            //btnSend.click();
            mDevice.pressBack();
            mDevice.pressBack();
        }else{
            /*
            onView(withText(R.string.email_activity_not_found_exception_text)).
                    inRoot(withDecorView(IsNot.not(Matchers.is(mActivity.getWindow().getDecorView())))).
                    check(matches(isDisplayed()));
                    */
            // Открылась смс
            mDevice.pressBack();
            mDevice.pressBack();
            UiObject btnOk = mDevice.findObject(new UiSelector().text("OK"));
            btnOk.click();
            mDevice.pressBack();
        }
    }

    final String IMAGE_PATH = "file:///android_asset/test_image.jpg";

    void setProductImageTestFromAssets(String productName){
        ContentResolver contentResolver = mActivity.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_PICTURE,
                IMAGE_PATH);
        contentResolver.update(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                contentValues, SL_ContentProvider.KEY_NAME + " = ?", new String[]{productName});
    }
}

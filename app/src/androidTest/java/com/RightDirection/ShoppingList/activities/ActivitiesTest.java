package com.RightDirection.ShoppingList.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
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
import org.junit.Rule;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
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
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
abstract class ActivitiesTest {

    static String mNewListName = "newTestShoppingList";
    final static String mNewProductNamePattern = "testNewProduct";
    final static String mNewCategoryNamePattern = "testNewCategory";
    static UiDevice mDevice = null;
    static MainActivity mActivity = null;

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
        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Выбираем "Настройки"
        onView(withText(mActivity.getString(R.string.action_settings))).perform(click());

        // Проверяем, что открылась форма настроек
        onView(withText(mActivity.getString(R.string.pref_cross_out_action))).check(matches(isDisplayed()));
    }

    static Matcher<View> isChildOfRecyclerViewItem(final Matcher<View> recyclerViewItem) {
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
    static Matcher<Object> withItemValue(final String value) {
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

    Matcher<View> recyclerViewItemWithText(final String itemText)
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

    Matcher<View> recyclerViewItemWithImage(final int imageId)
    {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        hasDescendant(withContentDescription(String.valueOf(imageId)))).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView with image id: " + imageId);
            }
        };
    }

    Matcher<View> recyclerViewItemWithImageAndText(final int imageId, final String itemText)
    {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        withId(R.id.productRepresent),
                        hasDescendant(withContentDescription(String.valueOf(imageId))),
                        hasDescendant(withText(itemText))).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView with image id: " + imageId
                        + " and with text: " + itemText);
            }
        };
    }

    Matcher<View> recyclerViewItemWithImageAndText(final String imageURI, final String itemText)
    {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        withId(R.id.productRepresent),
                        hasDescendant(withContentDescription(imageURI)),
                        hasDescendant(withText(itemText))).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView with image id: " + imageURI
                        + " and with text: " + itemText);
            }
        };
    }

    Matcher<View> recyclerViewItemWithImageAndTextForScrolling(final int imageId, final String itemText)
    {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return allOf(hasDescendant(withContentDescription(String.valueOf(imageId))),
                        hasDescendant(withText(itemText))).matches(view);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is descendant of a RecyclerView with image id: " + imageId
                        + " and with text: " + itemText);
            }
        };
    }

    Matcher<View> recyclerViewItemContainsText(final String subString)
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

    void addNewShoppingList(){
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

    void editNewShoppingList() {
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
        ListAdapter listAdapter = (ListAdapter) rv.getAdapter();
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            ListItem listItem = listAdapter.getItem(i);
            assertThat("Item is in the list", text, is(not(listItem.getName())));
        }
    }

    void addNewProduct(String name){
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

        pressBack();
    }

    void addNewCategory(){
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

    class RecyclerViewItemCountAssertion implements ViewAssertion {

        private final Matcher<Integer> matcher;

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
        }
    }
}

package com.RightDirection.ShoppingList.activities;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.MediumTest;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ProductActivitiesTest extends ActivitiesTest {

    @Test
    @MediumTest
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
        onView(withId(R.id.btnSave)).perform(click());
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
        onView(withId(R.id.btnSave)).perform(click());
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

}
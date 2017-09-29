package com.RightDirection.ShoppingList.activities;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SmallTest;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

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
                hasSibling(recyclerViewItemWithText(textForTyping))))
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
    }

    @Test
    @SmallTest
    public void testChoosingCategory() {
        String textForTyping;
        addNewCategory();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
        // Нажимаем кнопку вызова подменю
        openMainMenu();
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
        onView(withId(R.id.btnChooseCategory)).check(matches(withText(
                mActivity.getString(R.string.three_dots, mNewCategoryNamePattern))));
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));
        // Еще раз зайдем в продукт и убедимся, что категория сохранилась
        onView(recyclerViewItemWithText(textForTyping)).perform(click());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnChooseCategory)).check(matches(withText(
                mActivity.getString(R.string.three_dots, mNewCategoryNamePattern))));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @SmallTest
    public void testChoosingUnit() {
        String textForTyping;
        addNewUnit();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
        // Нажимаем кнопку вызова подменю
        openMainMenu();
        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());
        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));
        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());
        textForTyping = mNewProductNamePattern + "testProducts'3";
        onView(withId(R.id.etProductName)).perform(typeText(textForTyping));
        // Скроем клавиатуру
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        // Проверяем, что в поле Ед. измерения написано выражение по умолчанию
        onView(withId(R.id.btnUnit))
                .check(matches(withText(R.string.default_unit)));
        onView(withId(R.id.btnUnit)).perform(click());
        // Выбираем созданную ед. измерения
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePlusShortNamePattern))));
        onView(recyclerViewItemWithText(mNewUnitNamePlusShortNamePattern)).perform(click());
        // Проверяем, что категория отобразилась в активности
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern)));
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));
        // Еще раз зайдем в продукт и убедимся, что категория сохранилась
        onView(recyclerViewItemWithText(textForTyping)).perform(click());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnUnit)).check(matches(withText(mNewUnitShortNamePattern)));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @SmallTest
    public void testChangingPrice() {
        String textForTyping;
        // Нажимаем кнопку вызова подменю
        openMainMenu();
        // Выбираем "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());
        // Проверяем, что открылась активность "Список продуктов"
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).check(matches(isDisplayed()));
        // Нажимаем кнопку добавления нового продукта
        onView(withId(R.id.fabProductListAddProduct)).perform(click());
        textForTyping = mNewProductNamePattern + "testProducts'3";
        onView(withId(R.id.etProductName)).perform(typeText(textForTyping));
        // Скроем клавиатуру
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        // Проверяем, что в поле "Последняя цена" написано выражение по умолчанию
        onView(withId(R.id.etLastPrice))
                .check(matches(withHint(R.string.enter_price)));
        onView(withId(R.id.etLastPrice))
                .check(matches(withText("")));
        onView(withId(R.id.etLastPrice)).perform(clearText());
        onView(withId(R.id.etLastPrice)).perform(typeText("123.1"));
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что новый продукт отобразился в списке
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(textForTyping))));
        onView(recyclerViewItemWithText(textForTyping)).check(matches(isDisplayed()));
        // Еще раз зайдем в продукт и убедимся, что цена сохранилась
        onView(recyclerViewItemWithText(textForTyping)).perform(click());
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.etLastPrice)).check(matches(withText("123.10")));

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testShowClearImageButtons() {
        // Добавим новый товар ии открываем карточке товара
        addNewProduct(mNewProductNamePattern);
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern))));
        onView(recyclerViewItemWithText(mNewProductNamePattern)).perform(click());

        // Проверяем, что кнопок "Показать картинку" и "Очистить картинку" нет
        onView(withId(R.id.ibShowImage)).check(matches(not(isDisplayed()))); //  метод doesNotExist использовать нельзя, т.к. VISIBILITY = GONE все равно оставляет View в иерархии
        onView(withId(R.id.ibClearImage)).check(matches(not(isDisplayed())));

        // Выбираем картинку
        pressBack();
        pressBack();
        setProductImageTestFromAssets(mNewProductNamePattern);
        openMainMenu();
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern))));
        onView(recyclerViewItemWithText(mNewProductNamePattern)).perform(click());

        // Проверяем, что кнопки "Показать картинку" и "Очистить картинку" появились
        onView(withId(R.id.ibShowImage)).check(matches(isDisplayed()));
        onView(withId(R.id.ibClearImage)).check(matches(isDisplayed()));

        // Переходим в список товаров, проверяем, что новый товар отображается с картинкой
        pressBack();
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern))));
        timeout(500); // подождем пока картинка загрузится
        onView(recyclerViewItemWithImageAndText(IMAGE_PATH, mNewProductNamePattern))
                .check(matches(isDisplayed()));

        // Снова открываем товар
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern))));
        onView(recyclerViewItemWithText(mNewProductNamePattern)).perform(click());

        // Проверяем, что кнопки "Показать картинку" и "Очистить картинку" появились, отображается требуемая картинка
        onView(withId(R.id.ibShowImage)).check(matches(isDisplayed()));
        onView(withId(R.id.ibClearImage)).check(matches(isDisplayed()));
        onView(withContentDescription(IMAGE_PATH)).check(matches(isDisplayed()));

        // Нажимаем кнопку "Показать картинку", проверяем, что открылась активность с картинкой
        onView(withId(R.id.ibShowImage)).perform(click());
        // Убрали панель с заголовком
        //onView(withText(mActivity.getString(R.string.product_image_activity_title, mNewProductNamePattern))).check(matches(isDisplayed())); // проверяемость нужную активность по заголовку
        onView(withContentDescription(IMAGE_PATH)).check(matches(isDisplayed()));

        // Возвращаемся к карточке продукта
        pressBack();

        // Нажимаем на кнопку "Очистить картинку"
        onView(withId(R.id.ibClearImage)).perform(click());

        // Проверяем, что в поле картинки отображается картинка по умолчанию
        onView(withContentDescription(String.valueOf(android.R.drawable.ic_menu_crop))).check(matches(isDisplayed()));

        // Сохраняем товар и переходим к списку продуктов
        onView(withId(R.id.btnSave)).perform(click());

        // Проверяем, что в списке отображается товар без картинки
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern))));
        onView(recyclerViewItemWithImageAndText(R.drawable.ic_default_product_image, mNewProductNamePattern))
                .check(matches(isDisplayed()));
    }

}
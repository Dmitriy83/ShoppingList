package com.RightDirection.ShoppingList.activities;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.MediumTest;

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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;
import static org.hamcrest.Matchers.allOf;

public class CategoryActivitiesTest extends ActivitiesTest {

    @Test
    @MediumTest
    public void testCategories(){
        addNewCategory();

        // Проверим редактирование категории
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewCategoryNamePattern))));
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
                hasSibling(recyclerViewItemWithText(textForTyping))))
                .perform(click());
        // Категория более не должна отображаться в списке
        onView(recyclerViewItemWithText(textForTyping)).check(doesNotExist());

        // Нажимаем кнопку "Назад" и проверяем, что вернулись к основной активности
        pressBack();
        onView(withId(R.id.fabAddNewShoppingList)).check(matches(isDisplayed()));
    }

    @Test
    @MediumTest
    public void testCategoryImages() {
        // Добавляем новую категорию
        addNewCategory();
        // Открываем новую категорию
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Выбираем для нее картинку "Рыба"
        onView(withId(R.id.imgItemImage)).perform(click());
        onView(recyclerViewItemWithImage(R.drawable.category_fish)).perform(click());
        // Сохраняем категорию
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что в списке отображена категория с выбранной картинкой
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(recyclerViewItemWithImageAndTextForScrolling(
                        R.drawable.category_fish, mNewCategoryNamePattern)));
        onView(recyclerViewItemWithImageAndText(R.drawable.category_fish, mNewCategoryNamePattern))
                .check(matches(isDisplayed()));
        // Снова открываем категорию
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Нажимаем на картинку и нажимаем кнопку "Без категории"
        onView(withId(R.id.imgItemImage)).perform(click());
        onView(withId(R.id.btnCategoryEmpty)).perform(click());
        // Сохраняем категорию
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что в списке отображена категория с картинкой по умолчанию
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(recyclerViewItemWithImageAndTextForScrolling(
                        R.drawable.ic_default_product_image, mNewCategoryNamePattern)));
        onView(recyclerViewItemWithImageAndText(R.drawable.ic_default_product_image, mNewCategoryNamePattern))
                .check(matches(isDisplayed()));
        // Снова открываем категорию
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Выбираем картинку "Мясо"
        onView(withId(R.id.imgItemImage)).perform(click());
        onView(recyclerViewItemWithImage(R.drawable.category_meat)).perform(click());
        // Сохраняем категорию
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что в списке отображена категория с выбранной картинкой
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(recyclerViewItemWithImageAndTextForScrolling(
                        R.drawable.category_meat, mNewCategoryNamePattern)));
        onView(recyclerViewItemWithImageAndText(R.drawable.category_meat, mNewCategoryNamePattern))
                .check(matches(isDisplayed()));

        // Добавляем новый список покупок (при этом создаются два новых продукта)
        pressBack();
        addNewShoppingList();
        // Открываем список для редактирования
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        // Долгий клик на одном из новых товаров - переход в активность редактирования товара
        onView(recyclerViewItemWithText(mNewProductNamePattern + "1")).perform(longClick());
        // Присваиваем товару созданную в тесте категорию (картинку для товара не назначаем)
        onView(withId(R.id.etProductName)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnChooseCategory)).perform(click());
        onView(withId(R.id.rvCategories)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewCategoryNamePattern))));
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Сохраняем товар
        onView(withId(R.id.btnSave)).perform(click());
        // В списке редактирования новый товар должен отобразиться с картинкой "Мясо"
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(recyclerViewItemWithImageAndTextForScrolling(
                        R.drawable.category_meat, mNewProductNamePattern + "1")));
        onView(recyclerViewItemWithImageAndText(R.drawable.category_meat, mNewProductNamePattern + "1"))
                .check(matches(isDisplayed()));
        // Переходим в активность "В магазине" и проверяем, что картинка у товара также "Мясо"
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(recyclerViewItemWithImageAndTextForScrolling(
                        R.drawable.category_meat, mNewProductNamePattern + "1")));
        onView(recyclerViewItemWithImageAndText(R.drawable.category_meat, mNewProductNamePattern + "1"))
                .check(matches(isDisplayed()));

        // Переходим в активность "Товары"
        pressBack();

        // Назначаем товару картинку из assets
        setProductImageTestFromAssets(mNewProductNamePattern + "1");

        openMainMenu();
        onView(withText(mActivity.getString(R.string.action_edit_products_list))).perform(click());
        // Находим новый товар с картинкой из Assets
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern + "1"))));
        timeout(500); // подождем пока картинка загрузится
        onView(recyclerViewItemWithImageAndText(IMAGE_PATH, mNewProductNamePattern + "1"))
                .check(matches(isDisplayed()));
        pressBack();
        // Проверяемя, что товар отобразился с данной картинкой в активностях "Редактирование списка",
        //  "В магазине" и "Продукты"
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern + "1"))));
        timeout(500); // подождем пока картинка загрузится
        onView(recyclerViewItemWithImageAndText(IMAGE_PATH, mNewProductNamePattern + "1"))
                .check(matches(isDisplayed()));
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        onView(withId(R.id.rvProducts)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewProductNamePattern + "1"))));
        timeout(500); // подождем пока картинка загрузится
        onView(recyclerViewItemWithImageAndText(IMAGE_PATH, mNewProductNamePattern + "1"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyOrderTest(){
        // Добавляем новую категорию
        addNewCategory();
        // Открываем новую категорию
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        // Очищаем текст из поля Порядок
        onView(withId(R.id.etOrder)).perform(clearText());
        // Сохраняем
        onView(withId(R.id.btnSave)).perform(click());
        // Открываем категорию, проверяем, что порядок равен 100
        onView(recyclerViewItemWithText(mNewCategoryNamePattern)).perform(click());
        onView(withId(R.id.etOrder)).check(matches(withText("100")));
        pressBack();
    }
}
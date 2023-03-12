package com.RightDirection.ShoppingList.activities;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.filters.MediumTest;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.recyclerViewItemWithText;
import static org.hamcrest.Matchers.allOf;

public class UnitActivitiesTest extends ActivitiesTest {

    @Test
    @MediumTest
    public void testUnits(){
        addNewUnit();

        openMainMenu();
        onView(withText(mActivity.getString(R.string.action_edit_units_list))).perform(click());
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
                .scrollTo(hasDescendant(withText(mNewUnitNamePattern))));
        onView(recyclerViewItemWithText(mNewUnitNamePattern)).perform(click());
        String textForTyping = mNewUnitNamePattern + "Edited";
        onView(withId(R.id.etName)).perform(clearText());
        onView(withId(R.id.etName)).perform(typeText(textForTyping));
        onView(withId(R.id.btnSave)).perform(click());
        // Проверяем, что отредактированный продукт отобразился в списке
        onView(withId(R.id.rvUnits)).perform(RecyclerViewActions
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

        // Возвращаем настройки в значения по умолчанию
        setSettingsShowUnits(false);
    }
}
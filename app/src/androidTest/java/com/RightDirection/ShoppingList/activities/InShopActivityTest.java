package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.uiautomator.UiObjectNotFoundException;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;

public class InShopActivityTest extends ActivitiesTest {

    private void setSettingCrossOutProduct(){
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
    }

    private void setSettingNotCrossOutProduct(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean crossOutProduct = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_cross_out_action), true);
        if (crossOutProduct) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_cross_out_action))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    private void setSettingsShowCategories(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean showCategories = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_show_categories), true);
        if (!showCategories) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_show_categories))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    private void setSettingsNotShowCategories(){
        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean showCategories = sharedPref.getBoolean(mActivity.getString(R.string.pref_key_show_categories), true);
        if (showCategories) {
            // Установим нужную настройку
            openSettings();
            onView(withText(mActivity.getString(R.string.pref_show_categories))).perform(click());

            // Возвращаемся к основной активности
            pressBack();
        }
    }

    private void changeCountToNotInteger(){
        // Длинный клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());

        // В меню действий нажимаем кнопку редактирования списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        getEtCountViewInteraction(mNewProductNamePattern + "3")
                .perform(clearText())
                .perform(typeText("1.3"));
        onView(withId(R.id.action_save_list)).perform(click());
    }

    @LargeTest
    @Test
    public void testActivityInShop_CategoriesShowing() throws UiObjectNotFoundException {
        setSettingsShowCategories();
        // Создаем новый список покупок и редактируем его (чтобы получить три элемента)
        addNewShoppingList();
        editNewShoppingList();
        changeCountToNotInteger();

        testActivityInShop();
        inShopActivity_DeselectAll();
        inShopActivity_Filtered_DeselectAll();
        inShopActivity_SavingCheckedItems();
        inShopActivity_Filtered_SavingCheckedItems();
        inShopActivity_RemoveUnfilteredCheckedItemsFromListInDB();
        inShopActivity_RemoveFilteredCheckedItemsFromListInDB();
        inShopActivity_SendEmail();
        inShopActivity_LoadShoppingList();
    }

    @LargeTest
    @Test
    public void testActivityInShop_CategoriesNotShowing() throws UiObjectNotFoundException {
        setSettingsNotShowCategories();
        // Создаем новый список покупок и редактируем его (чтобы получить три элемента)
        addNewShoppingList();
        editNewShoppingList();
        changeCountToNotInteger();

        testActivityInShop();
        inShopActivity_DeselectAll();
        inShopActivity_Filtered_DeselectAll();
        inShopActivity_SavingCheckedItems();
        inShopActivity_Filtered_SavingCheckedItems();
        inShopActivity_RemoveUnfilteredCheckedItemsFromListInDB();
        inShopActivity_RemoveFilteredCheckedItemsFromListInDB();
        inShopActivity_SendEmail();
        inShopActivity_LoadShoppingList();
    }

    private void testActivityInShop() {
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();

        // Клик на новом списке покупок -> Переход к активности "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

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

        setSettingNotCrossOutProduct();

        // Клик на новом списке покупок -> Переход к активности "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Снимаем выеделение со всех товаров
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(click());
        }
        // Снова выделяем
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(click());
        }
        // Проверяем, что появилось окно с надписью "Победа!"
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));

        pressBack();
        // Снимаем выеделение со всех товаров (для следующего теста)
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(click());
        }

        // Возвращаемся к основной активности
        pressBack();
    }

    private void inShopActivity_SendEmail() throws UiObjectNotFoundException {
        // Переходим в активность "В магазине"
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Вычеркнем один товар, чтобы проверить, что передаются только не вычеркнутые
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).perform(swipeRight());

        // Нажимаем кнопку отправки списка покупок по почте
        openActionBarOverflowOrOptionsMenu(mActivity.getApplicationContext());
        onView(withText(mActivity.getString(R.string.share))).perform(click());

        checkEmailAppearing(
                mActivity.getString(R.string.json_file_identifier) + " '" + mNewListName + "'",
                "" + mNewProductNamePattern + "1, 1.0;"
                        //+ "\n" + mNewProductNamePattern + "2, 1.0;"
                        + "\n" + mNewProductNamePattern + "3, 1.3;");
    }

    private void inShopActivity_LoadShoppingList(){
        // Клик на новом списке покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Нажимаем на кнопку вызова подменю
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // В меню нажимаем кнопку отправки списка по почте
        onView(withText(R.string.load)).perform(click());

        // Открылась форма загрузки
        loadAndCheckList();
    }

    private void inShopActivity_DeselectAll(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();

        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Выделяем все элементы
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        pressBack();

        // Снимаем выделение со всех
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_deselect_all))).perform(click());

        // Опять выделяем. Проверяем отсутствие надписи.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(doesNotExist());

        // Снимаем выделение для следующего этапа
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_deselect_all))).perform(click());

        pressBack();
    }

    private void inShopActivity_Filtered_DeselectAll(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();

        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Устанавливаем фильтр
        onView(withId(R.id.action_filter)).perform(click());
        // Выделяем все элементы
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).perform(swipeRight());

        // Проверяем, что продукты не отображаются
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(doesNotExist());

        // Снимаем выделение со всех
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_deselect_all))).perform(click());

        // Опять выделяем. Проверяем отсутствие надписи.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(doesNotExist());

        // Проверяем, что отображается только один продукт
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(matches(isDisplayed()));

        // Снимаем выделение для следующего этапа
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_deselect_all))).perform(click());

        // Снимаем фильтр
        onView(withId(R.id.action_filter)).perform(click());

        pressBack();
    }

    private void inShopActivity_SavingCheckedItems(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();

        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Выделяем два элемента из трех
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());
        // Нажимаем кнопку назад
        pressBack();
        // Снова открываем список покупок и выделяем оставшийся элемент списка. Должна появиться надпись об окончании редактиирования списка.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        // Нажимаем кнопку назад
        pressBack();
        pressBack();
        // Снова открываем список покупок и снимаем выделение с одного из элементов списка.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeLeft());
        // Опять выделяем. Проверяем появление надписи.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        pressBack();
        // Снимаем выделение с одного из элементво и переходим к активности Редактирования списка
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeLeft());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        // Возвращаемся к активности В магазине и выделяем оставшийся элемент списка. Должна появиться надпись об окончании редактиирования списка.
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        pressBack();

        // Снимаем выеделение со всех товаров (для следующего теста)
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(swipeLeft());
        }
        pressBack();
    }

    private void inShopActivity_Filtered_SavingCheckedItems(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();

        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Устанавливаем фильтр
        onView(withId(R.id.action_filter)).perform(click());
        // Выделяем два элемента из трех
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());

        // Проверяем, что в списке отображается то, что нужно
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());

        // Нажимаем кнопку назад
        pressBack();
        // Снова открываем список покупок и выделяем оставшийся элемент списка. Должна появиться надпись об окончании редактиирования списка.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Проверяем, что в списке отображается то, что нужно (фильтр сохранился)
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());

        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        // Нажимаем кнопку назад
        pressBack();
        pressBack();

        // Снова открываем список покупок и снимаем выделение с одного из элементов списка.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());

        // Сначала необходимо снять фильтр
        onView(withId(R.id.action_filter)).perform(click());
        // Проверяем, что в списке отображается то, что нужно (фильтр снят)
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(matches(isDisplayed()));

        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeLeft());
        // Устанавливаем фильтр
        onView(withId(R.id.action_filter)).perform(click());
        // Опять выделяем. Проверяем появление надписи.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        pressBack();

        // Снимаем фильтр
        onView(withId(R.id.action_filter)).perform(click());
        // Снимаем выделение с одного из элементов и переходим к активности Редактирования списка
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeLeft());
        onView(withId(R.id.action_edit_shopping_list)).perform(click());

        // Возвращаемся к активности В магазине и выделяем оставшийся элемент списка. Должна появиться надпись об окончании редактиирования списка.
        onView(withId(R.id.action_go_to_in_shop_activity)).perform(click());

        // Проверяем, что в списке отображается то, что нужно (фильтр снят)
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(matches(isDisplayed()));

        // Устанавливаем фильтр
        onView(withId(R.id.action_filter)).perform(click());

        // Проверяем, что в списке отображается то, что нужно (фильтр сохранился)
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(matches(isDisplayed()));

        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());
        onView(withText(mActivity.getString(R.string.in_shop_ending_work_message))).check(matches(isDisplayed()));
        pressBack();

        // Снимаем выеделение со всех товаров (для следующего теста)
        onView(withId(R.id.action_filter)).perform(click());
        for (int i = 1; i <= 3; i++){
            onView(recyclerViewItemWithText(mNewProductNamePattern + i)).perform(swipeLeft());
        }
        pressBack();
    }

    private void inShopActivity_RemoveUnfilteredCheckedItemsFromListInDB(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();
        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Выделяем два элемента из трех
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());
        // Нажимаем кнопку Удалить вычеркнутые продукты
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_remove_checked))).perform(click());
        // Проверяем, что вычеркнутые продукты более не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());
        // Переходим в активность Редактирование списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        // Проверяем, что вычеркнутые продукты не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());
        // На всякий случай нажмем на текстовое поле, т.к. в разных версиях
        // андроид клавиатура может то показываться, то нет
        onView(withId(R.id.newItemFragment)).perform(click());
        // Нажимаем кнопку назад
        pressBack();
        pressBack();
        // Снова открываем список.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Проверяем, что вычеркнутые продукты не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());

        // Для следующего теста добавим удаленные товары в список
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        addProductInList(mNewProductNamePattern + 1, false);
        addProductInList(mNewProductNamePattern + 3, false, 1.3);
        onView(withId(R.id.action_save_list)).perform(click());
    }

    private void inShopActivity_RemoveFilteredCheckedItemsFromListInDB(){
        // Проверяем способ выделения в активности В магазине. При необходимости меняем на выделение свайпом.
        setSettingCrossOutProduct();
        // Переходим в активность В магазине
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Выделяем два элемента из трех
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).perform(swipeRight());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).perform(swipeRight());

        // Нажимаем кнопку фильтрации списка
        onView(withId(R.id.action_filter)).perform(click());

        // Нажимаем кнопку Удалить вычеркнутые продукты
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(mActivity.getString(R.string.action_remove_checked))).perform(click());
        // Проверяем, что вычеркнутые продукты более не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());

        // Нажимаем кнопку фильтрации списка
        onView(withId(R.id.action_filter)).perform(click());
        // Проверяем, что вычеркнутые продукты более не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());
        // Но отображается не вычеркнутый
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));

        // Еще раз нажмем кнопку фильтрации списка
        onView(withId(R.id.action_filter)).perform(click());
        // Проверяем, что вычеркнутые продукты более не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());
        // Но отображается не вычеркнутый
        onView(recyclerViewItemWithText(mNewProductNamePattern + 2)).check(matches(isDisplayed()));

        // Переходим в активность Редактирование списка
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        // Проверяем, что вычеркнутые продукты не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());
        // На всякий случай нажмем на текстовое поле, т.к. в разных версиях
        // андроид клавиатура может то показываться, то нет
        onView(withId(R.id.newItemFragment)).perform(click());
        // Нажимаем кнопку назад
        pressBack();
        pressBack();
        // Снова открываем список.
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
        // Проверяем, что вычеркнутые продукты не отображаются в списке.
        onView(recyclerViewItemWithText(mNewProductNamePattern + 1)).check(doesNotExist());
        onView(recyclerViewItemWithText(mNewProductNamePattern + 3)).check(doesNotExist());

        // Для следующего теста добавим удаленные товары в список
        onView(withId(R.id.action_edit_shopping_list)).perform(click());
        addProductInList(mNewProductNamePattern + 1, false);
        addProductInList(mNewProductNamePattern + 3, false, 1.3);
        onView(withId(R.id.action_save_list)).perform(click());
    }

    @Test
    @MediumTest
    public void inShopActivity_ChangeProductNameTest(){
        addNewShoppingList();
        onView(recyclerViewItemWithText(mNewListName)).perform(click());
        onView(withId(R.id.btnInShop)).perform(click());
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
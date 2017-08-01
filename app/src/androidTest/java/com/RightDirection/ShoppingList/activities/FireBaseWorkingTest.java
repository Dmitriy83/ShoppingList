package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.RightDirection.ShoppingList.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.RightDirection.ShoppingList.activities.CustomMatchers.*;
import static com.RightDirection.ShoppingList.utils.FirebaseUtil.userSignedIn;
import static org.junit.Assert.assertTrue;

public class FireBaseWorkingTest extends ActivitiesTest {

    private static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Test
    public void authorizationTest() throws UiObjectNotFoundException {
        authorizeAs("zhiharevtest1@gmail.com");

        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_profile))).perform(click());

        // Проверяем появление кнопки Выйти и нажимаем ее
        onView(withId(R.id.sign_out_button)).perform(click());

        // Проверяем появление кнопки Авторизация Гугл и снова авторизуемся
        onView(withId(R.id.launch_sign_in)).perform(click());
        UiObject btnUser = mDevice.findObject(new UiSelector().textContains("zhiharevtest1@gmail.com"));
        btnUser.click();
        timeout(1000);

        // Нажимаем кнопку Назад
        pressBack();

        // Открываем панель Навигации
        openMainMenu();
        // Проверяем отображение имени пользователя в заголовке
        onView(withText("Dmitriy Zhikharev")).check(matches(isDisplayed()));
        // Проверяем отсутствие пункта меню Войти
        onView(withText(mActivity.getString(R.string.action_sign_in))).check(doesNotExist());

        // Нажимаем на имени пользователя
        onView(withBackground(R.drawable.ic_drop_down_arrow)).check(matches(isDisplayed())); // перед нажатием стрелка смотрит вниз
        onView(withText("Dmitriy Zhikharev")).perform(click());
        // Проверяем, что элементы меню изменились, стрелка стала Вверх
        onView(withText(mActivity.getString(R.string.action_friends))).check(matches(isDisplayed()));
        onView(withBackground(R.drawable.ic_drop_up_arrow)).check(matches(isDisplayed())); // после нажатия стрелка смотрит вверх

        // Нажимаем Профиль
        onView(withText(mActivity.getString(R.string.action_profile))).perform(click());

        // Проверяем наличие кнопки Выйти
        onView(withId(R.id.sign_out_button)).check(matches(isDisplayed()));

        // Нажимаем Назад, открываем панель навигации, нажимаем на стрелку рядом с именем пользователя, нажимаем профиль, выходим - таким образом возвращаем программу в исходное состояние для дальнейшего тестирования.
        pressBack();
        openMainMenu();
        //onView(withBackground(R.drawable.ic_drop_down_arrow)).perform(click());
        onView(withText(mActivity.getString(R.string.action_profile))).perform(click());
        onView(withId(R.id.sign_out_button)).perform(click());
        pressBack();
    }

    @Test
    public void friendsTest() throws UiObjectNotFoundException, InterruptedException {

        authorizeAs("zhiharevtest1@gmail.com");

        cleanFriendsList();

        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_friends))).perform(click());

        // Добавляем нового друга zhiharevtest2@gmail.com. Проверяем, что он появился в списке через 3 сек.
        onView(withId(R.id.etFriendEmail)).perform(typeText("zhiharevtest2@gmail.com"));
        onView(withId(R.id.btnSearchFriend)).perform(click());
        timeout(500);
        onView(recyclerViewItemWithText("Dmitriy Zhikharev")).check(matches(isDisplayed()));

        // Добавляем нового друга zhiharevtest3@gmail.com. Проверяем, что он в списке
        onView(withId(R.id.etFriendEmail)).perform(clearText());
        onView(withId(R.id.etFriendEmail)).perform(typeText("zhiharevtest3@gmail.com"));
        onView(withId(R.id.btnSearchFriend)).perform(click());
        timeout(500);
        // Проверять не будем, т.к. название одинаковое

        // Удаляем zhigarevtest2
        onView(withId(R.id.rvFriends))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.imgDelete)));

        // Удаляем zhiharevtest3
        onView(withId(R.id.rvFriends))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.imgDelete)));

        // Проверяем, что список пустой
        onView(withText(R.string.friends_no_data_text)).check(matches(isDisplayed()));

        // Кнопка Назад.
        pressBack();
    }

    /**
     * Рекурсивная функция для очистки списка пользователей (друзей, черного списка и т.п.)
     */
    @SuppressWarnings("InfiniteRecursion")
    private void pressRemoveUsersIfExist(int usersList) {
        try {
            onView(withId(usersList))
                    .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.imgDelete)));
            pressRemoveUsersIfExist(usersList);
        }catch (Exception e) {
            // Список пуст
        }
    }

    @Test
    public void sendingShoppingListEmptyFriendsAndBlackListsTest() throws UiObjectNotFoundException {
        // Авторизуемся, как zhiharevtest1@gmail.com
        authorizeAs("zhiharevtest1@gmail.com");

        // Проверяем, что список друзей и черный список пусты. При необходимости последовательно удаляем элементы.
        cleanFriendsList();
        cleanBlackList();

        // Выходим из этого пользователя, авторизуемся под zhiharevtest2@gmail.com
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_profile))).perform(click());
        onView(withId(R.id.sign_out_button)).perform(click());
        pressBack();
        authorizeAs("zhiharevtest2@gmail.com");

        // Повторяем проверку
        cleanFriendsList();
        cleanBlackList();

        // Добавляем новый список покупок
        addNewShoppingList();
        sendAndRemoveShoppingListEmptyFriendsList();

        // Выход, авторизуемся под первым пользователем.
        authorizeAs("zhiharevtest1@gmail.com");

        // Открываем панель навинации, щелчок на имени пользователя, пункт меню Получить списки покупок.
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_receive_shopping_lists))).perform(click());
        timeout(500);
        onView(withText(mActivity.getString(R.string.add_to_friends_and_receive_list))).perform(click());
        timeout(500);
        pressBack();

        // Проверяем, что список покупок появился на основном экране
        onView(recyclerViewItemContainsText(mNewListName)).check(matches(isDisplayed()));
    }

    private void cleanBlackList() {
        // Переходим в список друзей черещ панель навигации
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_black_list))).perform(click());

        // Если друзья в списке есть, то удаляем их
        pressRemoveUsersIfExist(R.id.rvBlackList);
        pressBack();
    }

    private void cleanFriendsList() {
        // Переходим в список друзей черещ панель навигации
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_friends))).perform(click());

        // Если друзья в списке есть, то удаляем их
        pressRemoveUsersIfExist(R.id.rvFriends);
        pressBack();
    }

    private void authorizeAs(String account) throws UiObjectNotFoundException {
        // Проверить, что есть подключение к интернету. Если нет, прервать тест;
        assertTrue(isConnected(mActivity));

        // Проверить, что пользователь не авторизован. Если авторизован, выйти.
        if (userSignedIn(mActivity)){
            timeout(500);
            openUserSubmenu();
            onView(withText(mActivity.getString(R.string.action_profile))).perform(click());
            onView(withId(R.id.sign_out_button)).perform(click());
            pressBack();
        }

        // Нажимаем кнопку Войти
        openMainMenu();
        onView(withText(mActivity.getString(R.string.action_sign_in))).perform(click());

        // Нажимаем кнопку Авторизация Гугл
        onView(withId(R.id.launch_sign_in)).perform(click());

        // Выбираем пользователя zhiharevtest1@gmail.com
        UiObject btnUser = mDevice.findObject(new UiSelector().textContains(account));
        btnUser.click();
        timeout(1000);
        pressBack();
    }

    @Test
    public void sendingShoppingListFriendsAndBlackListsNotEmptyTest() throws UiObjectNotFoundException {
        // Авторизуемся, как zhiharevtest1@gmail.com
        authorizeAs("zhiharevtest1@gmail.com");

        // Проверяем, что список друзей и черный список пусты. При необходимости последовательно удаляем элементы.
        cleanFriendsList();
        cleanBlackList();

        // Выходим из этого пользователя, авторизуемся под zhiharevtest2@gmail.com
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_profile))).perform(click());
        onView(withId(R.id.sign_out_button)).perform(click());
        pressBack();
        authorizeAs("zhiharevtest2@gmail.com");
        timeout(500);

        // Повторяем проверку
        cleanFriendsList();
        cleanBlackList();

        // Добавляем друга zhiharevtest1@gmail.com
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_friends))).perform(click());
        onView(withId(R.id.etFriendEmail)).perform(typeText("zhiharevtest1@gmail.com"));
        onView(withId(R.id.btnSearchFriend)).perform(click());
        pressBack();
        pressBack();

        // Добавляем новый список покупок и отправляем его другу
        addNewShoppingList();
        sendAndRemoveShoppingListNotEmptyFriendsList();

        // Выход, авторизуемся под первым пользователем.
        authorizeAs("zhiharevtest1@gmail.com");
        timeout(500);

        // Добавляем в черный список zhiharevtest2@gmail.com
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_receive_shopping_lists))).perform(click());
        timeout(500);
        onView(withText(mActivity.getString(R.string.add_to_black_list_and_decline_list))).perform(click());
        timeout(500);
        pressBack();

        // Проверяем, что список покупок не появился на основном экране
        onView(recyclerViewItemContainsText(mNewListName)).check(doesNotExist());

        // Повторяем получение - должен быть тот же результат.
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_receive_shopping_lists))).perform(click());
        timeout(500);
        pressBack();
        onView(recyclerViewItemContainsText(mNewListName)).check(doesNotExist());

        // Уберем пользователя из черного списка, отправим и проверим получение.
        cleanBlackList();
        authorizeAs("zhiharevtest2@gmail.com");
        timeout(500);
        addNewShoppingList();
        sendAndRemoveShoppingListNotEmptyFriendsList();
        authorizeAs("zhiharevtest1@gmail.com");
        timeout(500);
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_receive_shopping_lists))).perform(click());
        timeout(500);
        onView(withText(mActivity.getString(R.string.add_to_friends_and_receive_list))).perform(click());
        timeout(500);
        pressBack();
        onView(recyclerViewItemContainsText(mNewListName)).check(matches(isDisplayed()));
    }

    private void sendAndRemoveShoppingListNotEmptyFriendsList() {
        // Длинный клик, кнопка отправить другу.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_send_to_friend)).perform(click());

        // Отправляем пользователю zhiharevtest1@gmail.com.
        onView(recyclerViewItemWithText("Dmitriy Zhikharev")).perform(click());
        timeout(500);
        pressBack();

        // Удалим список покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.imgDelete)).perform(click());
        onView(withText(mActivity.getString(R.string.ok))).perform(click());
    }

    private void sendAndRemoveShoppingListEmptyFriendsList() {
        // Длинный клик, кнопка отправить другу.
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.action_send_to_friend)).perform(click());

        // Отправляем пользователю zhiharevtest1@gmail.com.
        onView(withId(R.id.fabAddNewFriends)).perform(click());
        onView(withId(R.id.etFriendEmail)).perform(typeText("zhiharevtest1@gmail.com"));
        onView(withId(R.id.btnSearchFriend)).perform(click());
        timeout(500);
        pressBack();
        pressBack();
        onView(recyclerViewItemWithText("Dmitriy Zhikharev")).perform(click());
        timeout(500);
        pressBack();

        // Удалим список покупок
        onView(recyclerViewItemWithText(mNewListName)).perform(longClick());
        onView(withId(R.id.imgDelete)).perform(click());
        onView(withText(mActivity.getString(R.string.ok))).perform(click());
    }

    private void openUserSubmenu(){
        openMainMenu();
        try {
            onView(withBackground(R.drawable.ic_drop_down_arrow)).perform(click());
        }catch(Exception e){
            // При прохождении теста иногда программа не успевает закрыть подменю пользователя. Поэтому здесь ошибку показывать не будем.
        }
    }

    @Test
    public void enterWrongFriendEmailTest() throws UiObjectNotFoundException {
        // Открываем панель навигации. Если пользователь авторизован, выходим. Авторизуемся под zhiharevtest1@gmail.com.
        authorizeAs("zhiharevtest1@gmail.com");
        // Заходим в список Друзей.
        openUserSubmenu();
        onView(withText(mActivity.getString(R.string.action_friends))).perform(click());
        // Набираем в поле поиска "wrongemail". Нажимаем добавить.
        onView(withId(R.id.etFriendEmail)).perform(typeText("wrongemail"));
        onView(withId(R.id.btnSearchFriend)).perform(click());
        // Должно появиться диалоговое окно с вопросом "Пользователь не зарегистрирован. Отправить приглашение другу?". Проверяем текст вопроса.
        onView(withText(mActivity.getString(R.string.wrong_email_dialog_question))).check(matches(isDisplayed()));
        // Нажимаем Нет.
        onView(withText(mActivity.getString(R.string.no))).perform(click());
        // Снова нажимаем кнопку Найти.
        onView(withId(R.id.btnSearchFriend)).perform(click());
        // В диалоговом окне выбираем Да.
        onView(withText(mActivity.getString(R.string.yes))).perform(click());
        timeout(1000);
        // Проверяем появление окна приглашения (с помощью UiAutomator). Текст приглашения - "Это приложение - электронный список покупок. Скачай и зарегистрируйся ;)".
        UiObject txtInvitation = mDevice.findObject(new UiSelector().text(mActivity.getString(R.string.friend_invitation_question)));
        txtInvitation.exists();
        mDevice.pressBack();
        pressBack();
    }
}

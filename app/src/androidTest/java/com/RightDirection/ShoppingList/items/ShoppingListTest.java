package com.RightDirection.ShoppingList.items;

import android.support.test.rule.ActivityTestRule;
import android.test.mock.MockContext;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ShoppingListTest {

    private static MainActivity mActivity = null;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();    }

    @Test
    public void loadProductsFromString() throws Exception {
        // Тесты должны включать следующие проверки после загрузки:
        // 1) Проверка количества созданных продуктов;
        // 2) Проверка созданных продуктов по имени и количеству.

        MockContext mockContext = new MockContext();
        String divider = mActivity.getString(R.string.divider);
        String productDivider = mActivity.getString(R.string.product_divider);
        Product product;
        ShoppingList testSL;
        String loadString;

        // Идеальный вариант: "test1, 2; test2, 1; test3, 5; test4, 1"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + " 2" + productDivider + " test2" + divider + " 1"
                + productDivider + " test3" + divider + " 5" + productDivider + " test4"
                + divider + " 1";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 4 продукта
        assertEquals(4, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);
        // Есть продукт test4 с количеством 1
        product = testSL.getProducts().get(3);
        assertEquals("test4", product.getName());
        assertEquals(1, product.getCount(), 0);


        // Вариант с различным сочетанием пробелов: "  test1,2;test2,   1;test3, 5   ; test4, 1   "
        testSL = new ShoppingList(-1, "", null);
        loadString = "  test1" + divider + "2" + productDivider + "test2" + divider + "   1"
                + productDivider + "test3" + divider + " 5   " + productDivider + " test4"
                + divider + " 1   ";
        testSL.loadProductsFromString(mActivity, loadString);
        // Проверки те же, что и в предыдущем варианте
        // Должно быть загружено 4 продукта
        assertEquals(4, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);
        // Есть продукт test4 с количеством 1
        product = testSL.getProducts().get(3);
        assertEquals("test4", product.getName());
        assertEquals(1, product.getCount(), 0);


        // Вариант с дробным и некорректным (в том числе отрицательным) количеством
        // (если некорректное, то указываем 1): "  test1,2.5;test2,   7.;test3, ...5   ; test4, -5"
        testSL = new ShoppingList(-1, "", null);
        loadString = "  test1" + divider + "2.5" + productDivider + "test2" + divider + "   7."
                + productDivider + "test3" + divider + " ...5   " + productDivider + " test4"
                + divider + " -5";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 4 продукта
        assertEquals(4, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2.5
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2.5, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(7, product.getCount(), 0);
        // Есть продукт test3 с количеством 1
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test4 с количеством 1
        product = testSL.getProducts().get(3);
        assertEquals("test4", product.getName());
        assertEquals(1, product.getCount(), 0);


        // Вариант с некорректным именем внутри строки: "  test1,2;  ,   1;, 5   ; t, 1"
        testSL = new ShoppingList(-1, "", null);
        loadString = "  test1" + divider + "2" + productDivider + "  " + divider + "   1"
                + productDivider + "" + divider + " 5   " + productDivider + " t"
                + divider + " 1";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 2 продукта
        assertEquals(2, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);
        // Есть продукт t с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("t", product.getName());
        assertEquals(1, product.getCount(), 0);


        // Вариант с некорректным именем по краям строки: ",2;test2,   1;test3, 5   ;, 1"
        testSL = new ShoppingList(-1, "", null);
        loadString = "" + divider + "2" + productDivider + "test2" + divider + "   1"
                + productDivider + "test3" + divider + " 5   " + productDivider + ""
                + divider + " 1";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 2 продукта
        assertEquals(2, testSL.getProducts().size());
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(0);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(1);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);


        // Вариант с разделитеялми продуктов по краям строки: ";test2,   1;test3, 5   ;"
        testSL = new ShoppingList(-1, "", null);
        loadString = productDivider + "test2" + divider + "   1"
                + productDivider + "test3" + divider + " 5   " + productDivider;
        testSL.loadProductsFromString(mActivity, loadString);
        // Проверки те же, что и в предыдущем варианте
        // Должно быть загружено 2 продукта
        assertEquals(2, testSL.getProducts().size());
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(0);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(1);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);


        // Вариант с пустой строкой: ""
        testSL = new ShoppingList(-1, "", null);
        loadString = "";
        testSL.loadProductsFromString(mActivity, loadString);
        assertNull(testSL.getProducts());

        // Вариант с пустой строкой с пробелами: "     "
        testSL = new ShoppingList(-1, "", null);
        loadString = "     ";
        testSL.loadProductsFromString(mActivity, loadString);
        assertNull(testSL.getProducts());

        // Вариант с пустой строкой с разделителями: ";;;;"
        testSL = new ShoppingList(-1, "", null);
        loadString = "     ";
        testSL.loadProductsFromString(mActivity, loadString);
        assertNull(testSL.getProducts());

        // Вариант с пустой строкой с разделителями: ";,,,;,;"
        testSL = new ShoppingList(-1, "", null);
        loadString = "     ";
        testSL.loadProductsFromString(mActivity, loadString);
        assertNull(testSL.getProducts());

        // Вариант с одним товаром: "test1,2"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + "2";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должен быть загружен 1 продукт
        assertEquals(1, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);

        // Вариант с одним товаром: "test1,2;"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + "2" + productDivider;
        testSL.loadProductsFromString(mActivity, loadString);
        // Должен быть загружен 1 продукт
        assertEquals(1, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);

        // Вариант с одним товаром без количества: "test1"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должен быть загружен 1 продукт
        assertEquals(1, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(1, product.getCount(), 0);

        // Вариант с несколькими товарами, в середине товар без количества: "test1, 2; test2; test3, 5;"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + " 2" + productDivider + " test2"
                + productDivider + " test3" + divider + " 5" + productDivider;
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 4 продукта
        assertEquals(3, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);

        // Еще один вариант с некорректным количеством: "test1, ,2; test2,; test3, ,,,5;"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + " ,2" + productDivider + " test2" + divider + ","
                + productDivider + " test3" + divider + " ,,,5" + productDivider;
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 4 продукта
        assertEquals(3, testSL.getProducts().size());
        // Есть продукт test1 с количеством 1
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 1
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(1, product.getCount(), 0);

        // Вариант, когда явно указано количество, равное 0: "test1,    0   "
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + "    0   ";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должен быть загружен 1 продукт
        assertEquals(1, testSL.getProducts().size());
        // Есть продукт test1 с количеством 0
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(0, product.getCount(), 0);

        // Вариант со знаками переноса: "test1, 2; test2, 1; test3, 5; test4, 1"
        testSL = new ShoppingList(-1, "", null);
        loadString = "test1" + divider + " 2" + productDivider + "\n" + " test2" + divider + " 1"
                + productDivider + "\n" + " test3" + divider + " 5" + productDivider + " test4"
                + divider + " 1";
        testSL.loadProductsFromString(mActivity, loadString);
        // Должно быть загружено 4 продукта
        assertEquals(4, testSL.getProducts().size());
        // Есть продукт test1 с количеством 2
        product = testSL.getProducts().get(0);
        assertEquals("test1", product.getName());
        assertEquals(2, product.getCount(), 0);
        // Есть продукт test2 с количеством 1
        product = testSL.getProducts().get(1);
        assertEquals("test2", product.getName());
        assertEquals(1, product.getCount(), 0);
        // Есть продукт test3 с количеством 5
        product = testSL.getProducts().get(2);
        assertEquals("test3", product.getName());
        assertEquals(5, product.getCount(), 0);
        // Есть продукт test4 с количеством 1
        product = testSL.getProducts().get(3);
        assertEquals("test4", product.getName());
        assertEquals(1, product.getCount(), 0);
    }
}
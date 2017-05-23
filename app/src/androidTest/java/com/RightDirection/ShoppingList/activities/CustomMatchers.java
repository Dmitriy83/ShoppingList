package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IListItem;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.intent.Checks.checkArgument;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

class CustomMatchers {
    private static boolean sameBitmap(Context context, Drawable drawable, int resourceId) {
        Drawable otherDrawable;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            otherDrawable = context.getResources().getDrawable(resourceId);
        } else{
            otherDrawable = context.getDrawable(resourceId);
        }
        if (drawable == null || otherDrawable == null) {
            return false;
        }
        if (drawable instanceof StateListDrawable && otherDrawable instanceof StateListDrawable) {
            drawable = drawable.getCurrent();
            otherDrawable = otherDrawable.getCurrent();
        }
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap otherBitmap = ((BitmapDrawable) otherDrawable).getBitmap();
            return bitmap.sameAs(otherBitmap);
        }
        return false;
    }

    static Matcher<View> withBackground(final int resourceId) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                return sameBitmap(view.getContext(), view.getBackground(), resourceId);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has background resource " + resourceId);
            }
        };
    }

    /**
     * Процедура необходима для поиска объектов класса ListItem в BaseListAdapter по имени
     */
    @SuppressWarnings("unused")
    static Matcher<Object> withItemValue(final String value) {
        return new BoundedMatcher<Object, IListItem>(IListItem.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has value " + value);
            }

            @Override
            public boolean matchesSafely(IListItem item) {
                return item.getName().equals(String.valueOf(value));
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

    static Matcher<View> recyclerViewItemWithText(final String itemText)
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

    static Matcher<View> recyclerViewItemWithImage(final int imageId)
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

    static Matcher<View> recyclerViewItemWithImageAndText(final int imageId, final String itemText)
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

    static Matcher<View> recyclerViewItemWithImageAndText(final String imageURI, final String itemText)
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

    static Matcher<View> recyclerViewItemWithImageAndTextForScrolling(final int imageId, final String itemText)
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

    static Matcher<View> recyclerViewItemContainsText(final String subString)
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

    static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}

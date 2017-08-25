package com.RightDirection.ShoppingList.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

    private final Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]*+((\\.[0-9]?)?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher = mPattern.matcher(dest);
        if(!matcher.matches())
            return "";
        return null;
    }
}
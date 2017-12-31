package com.RightDirection.ShoppingList.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.bumptech.glide.Glide;

public class InShopHelp1Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_in_shop1, container, false);
        ImageView imgHelpProductCrossOut = view.findViewById(R.id.imgHelpProductCrossOut);
        Glide.with(this).load(R.drawable.help_product_cross_out).into(imgHelpProductCrossOut);
        return view;
    }
}

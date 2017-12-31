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

public class InShopHelp2Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_in_shop2, container, false);
        ImageView imgHelpEditButton = view.findViewById(R.id.imgHelpEditButton);
        Glide.with(this).load(R.drawable.help_edit_button).into(imgHelpEditButton);
        return view;
    }
}

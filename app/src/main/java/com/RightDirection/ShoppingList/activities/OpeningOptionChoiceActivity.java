package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.ShoppingList;

public class OpeningOptionChoiceActivity extends AppCompatActivity {

    private ShoppingList mShoppingList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_option_choice);

        mContext = this; // для использования в обработчиках нажатия клавиш

        mShoppingList = getIntent().getParcelableExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue() );

        Button btnInShop = findViewById(R.id.btnInShop);
        if (btnInShop != null) btnInShop.setOnClickListener(onBtnInShopClick);

        Button btnEdit = findViewById(R.id.btnEdit);
        if (btnEdit != null) btnEdit.setOnClickListener(onBtnEditClick);
    }

    private final View.OnClickListener onBtnInShopClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mShoppingList.startInShopActivity(mContext);
        }
    };

    private final View.OnClickListener onBtnEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mShoppingList.startEditingActivity(mContext);
        }
    };
}
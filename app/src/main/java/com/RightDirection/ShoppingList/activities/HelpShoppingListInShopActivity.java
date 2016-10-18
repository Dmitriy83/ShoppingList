package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.R;

public class HelpShoppingListInShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_shopping_list_in_shop);

        Button btnGotIt = (Button) findViewById(R.id.btnGotIt);
        if (btnGotIt != null) btnGotIt.setOnClickListener(onBtnGotItClick);
    }

    private final View.OnClickListener onBtnGotItClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getApplicationContext().getString(R.string.pref_key_show_help_screens), false);
            editor.apply();
            finish();
        }
    };
}
package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.fragments.InShopHelp1Fragment;
import com.RightDirection.ShoppingList.fragments.InShopHelp2Fragment;

public class HelpShoppingListInShopActivity extends AppCompatActivity {

    private Fragment fragment1;
    private Fragment fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_shopping_list_in_shop);

        fragment1 = new InShopHelp1Fragment();
        fragment2 = new InShopHelp2Fragment();

        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment1, null)
                    .addToBackStack(null)
                    .commit();

            fragmentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onFragmentContainerClick(); }
            });
        }

        Button btnGotIt = findViewById(R.id.btnGotIt);
        if (btnGotIt != null) btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onBtnGotItClick(); }
        });
    }

    private void onFragmentContainerClick() {
        // Заменим фрагмент
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment1.isVisible()){
            ft.replace(R.id.fragment_container, fragment2, null).addToBackStack(null);
        }else{
            ft.replace(R.id.fragment_container, fragment1, null).addToBackStack(null);
        }
        ft.commit();
    }

    private void onBtnGotItClick() {
        setDoNotShowHelp();
        if (fragment2.isVisible()){ finish(); }

        // Заменим на следующий фрагмент
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment2, null)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // Если в стеке всего один фрагмент, то не будем его убирать (иначе останется на форме одна кнопка), а закроем активность
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        setDoNotShowHelp();
        super.onPause();
    }

    private void setDoNotShowHelp() {
        if (fragment2.isVisible()){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getApplicationContext().getString(R.string.pref_key_show_help_screens), false);
            editor.apply();
        }
    }
}
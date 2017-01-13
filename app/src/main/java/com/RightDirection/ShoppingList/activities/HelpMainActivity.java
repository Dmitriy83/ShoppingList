package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.views.FragmentMainHelp1;

public class HelpMainActivity extends AppCompatActivity {

    //private Fragment fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_main);

        Fragment fragment1 = new FragmentMainHelp1();
        //fragment2 = new FragmentInShopHelp2();

        FrameLayout fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
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

            //fragmentContainer.setOnClickListener(onFragmentContainerClick);
        }

        Button btnGotIt = (Button) findViewById(R.id.btnGotIt);
        if (btnGotIt != null) btnGotIt.setOnClickListener(onBtnGotItClick);
    }

    /*
    private final View.OnClickListener onFragmentContainerClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Заменим фрагмент
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (fragment1.isVisible()){
                ft.replace(R.id.fragment_container, fragment2, null);
            }else{
                ft.replace(R.id.fragment_container, fragment1, null);
            }
            ft.addToBackStack(null).commit();
        }
    };
    */

    private final View.OnClickListener onBtnGotItClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getApplicationContext().getString(R.string.pref_key_show_help_in_main_activity), false);
            editor.apply();

            finish();

            /*
            if (fragment2.isVisible()){
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getApplicationContext().getString(R.string.pref_key_show_help_screens), false);
                editor.apply();

                finish();
            }

            // Заменим на следующий фрагмент
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment2, null)
                    .commit();
                    */
        }
    };
}
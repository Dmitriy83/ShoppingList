package com.RightDirection.ShoppingList.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.fragments.MainHelp1Fragment;

public class HelpMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_main);

        Fragment fragment1 = new MainHelp1Fragment();

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
        }

        Button btnGotIt = findViewById(R.id.btnGotIt);
        if (btnGotIt != null) btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onBtnGotItClick(); }
        });
    }

    private void onBtnGotItClick() {
        finish();
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getApplicationContext().getString(R.string.pref_key_show_help_in_main_activity), false);
        editor.apply();
        super.onPause();
    }
}
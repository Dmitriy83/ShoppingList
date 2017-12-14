package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.RightDirection.ShoppingList.R;

public class DoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        LinearLayout layout = findViewById(R.id.done_container);
        if (layout != null) layout.setOnClickListener(onLayoutClick);
    }

    private final View.OnClickListener onLayoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}
package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.items.ShoppingList;

public class LoadShoppingListActivity extends AppCompatActivity {

    // Лист покупок для загрузки
    private ShoppingList mShoppingList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_shopping_list);

        mContext = this;

        Intent sourceIntent = getIntent();
        mShoppingList = sourceIntent.getParcelableExtra(String.valueOf(R.string.shopping_list));

        Button btnGotIt = (Button) findViewById(R.id.btnLoad);
        if (btnGotIt != null) btnGotIt.setOnClickListener(onBtnLoadClick);
    }

    private final View.OnClickListener onBtnLoadClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText etText = (EditText)findViewById(R.id.etTextForLoading);
            if (etText == null) finish();

            boolean isNewList = (mShoppingList.getId() == -1);

            mShoppingList.loadProductsFromString(mContext, etText.getText().toString());
            mShoppingList.addNotExistingProductsToDB(mContext);
            if (!isNewList) mShoppingList.updateInDB(mContext);

            // Откроем активность редактирования списка покупок
            Intent intent = new Intent(mContext, ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_list), isNewList);
            intent.putExtra(String.valueOf(R.string.list_id), mShoppingList.getId());
            intent.putExtra(String.valueOf(R.string.list_name), mShoppingList.getName());
            if (isNewList) intent.putExtra(String.valueOf(R.string.shopping_list_items), mShoppingList.getProducts());
            mContext.startActivity(intent);

            finish();
        }
    };
}
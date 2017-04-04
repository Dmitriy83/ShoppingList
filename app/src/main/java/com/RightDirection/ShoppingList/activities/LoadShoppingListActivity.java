package com.RightDirection.ShoppingList.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.ShoppingList;

public class LoadShoppingListActivity extends BaseActivity {

    // Лист покупок для загрузки
    private ShoppingList mShoppingList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_shopping_list);

        mContext = this;

        Intent sourceIntent = getIntent();
        mShoppingList = sourceIntent.getParcelableExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue());

        Button btnGotIt = (Button) findViewById(R.id.btnLoad);
        if (btnGotIt != null) btnGotIt.setOnClickListener(onBtnLoadClick);
    }

    private final View.OnClickListener onBtnLoadClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText etText = (EditText)findViewById(R.id.etTextForLoading);
            if (etText == null || etText.getText() == null) finish();

            assert etText != null;
            mShoppingList.loadProductsFromString(mContext, etText.getText().toString());
            mShoppingList.addNotExistingProductsToDB(mContext);
            if (!mShoppingList.isNew) mShoppingList.updateInDB(mContext);

            // Откроем активность редактирования списка покупок
            mShoppingList.startEditingActivity(mContext);
            Intent intent = new Intent(mContext, ShoppingListEditingActivity.class);
            intent.putExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue() , mShoppingList);
            if (mShoppingList.isNew) intent.putExtra(EXTRAS_KEYS.PRODUCTS.getValue(), mShoppingList.getProducts());
            mContext.startActivity(intent);

            finish();
        }
    };
}
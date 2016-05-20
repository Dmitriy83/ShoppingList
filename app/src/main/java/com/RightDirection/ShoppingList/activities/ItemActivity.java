package com.RightDirection.ShoppingList.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;

public class ItemActivity extends AppCompatActivity{

    public boolean isNewItem;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_form);

        // Получим значения из переданных параметров
        Intent sourceIntent = getIntent();
        isNewItem = sourceIntent.getBooleanExtra(String.valueOf(R.string.is_new_list), true);
        itemId = sourceIntent.getStringExtra(String.valueOf(R.string.list_id));
        String name = sourceIntent.getStringExtra(String.valueOf(R.string.name));
        EditText etProductName = (EditText) findViewById(R.id.etProductName);
        if (etProductName != null) {
            etProductName.setText(name);
        }

        // Добавим обработчики кликов по кнопкам
        Button btnSaveProduct = (Button) findViewById(R.id.btnSaveProduct);
        if (btnSaveProduct != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSaveProduct.setTransformationMethod(null);
            btnSaveProduct.setOnClickListener(onBtnSaveProductClick);
        }

        // Установим заголовок формы
        if (isNewItem){
            setTitle(getString(R.string.new_product));
        }
        else{
            setTitle(getString(R.string.product_title));
        }

        // TODO: Сделать так, чтобы при открытии клавиатуры не скрывалась кнопка сохранения
    }

    private View.OnClickListener onBtnSaveProductClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();

            EditText etProductName = (EditText) findViewById(R.id.etProductName);
            if (etProductName != null) {
                contentValues.put(ShoppingListContentProvider.KEY_NAME, etProductName.getText().toString());
                if (isNewItem) {
                    contentResolver.insert(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues);
                } else {
                    contentResolver.update(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, contentValues, "_id = " + itemId, null);
                }
            }

            finish();
        }
    };


}


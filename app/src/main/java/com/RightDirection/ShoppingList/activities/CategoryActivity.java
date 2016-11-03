package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.items.Category;

public class CategoryActivity extends AppCompatActivity{

    private boolean mIsNewCategory;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_editing);

        if (savedInstanceState != null){
            // Восстановим объект из сохраненных значений
            mIsNewCategory = savedInstanceState.getBoolean(String.valueOf(R.string.is_new_item), false);
            mCategory = savedInstanceState.getParcelable(String.valueOf(R.string.category));
        }else{
            // Получим значения из переданных параметров
            Intent sourceIntent = getIntent();
            mIsNewCategory = sourceIntent.getBooleanExtra(String.valueOf(R.string.is_new_item), true);
            mCategory = sourceIntent.getParcelableExtra(String.valueOf(R.string.category));
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mIsNewCategory){
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etCategoryName = (EditText) findViewById(R.id.etCategoryName);
        if (etCategoryName != null) etCategoryName.setText(mCategory.getName());

        EditText etCategoryOrder = (EditText) findViewById(R.id.etOrder);
        if (etCategoryOrder != null) etCategoryOrder.setText(String.valueOf(mCategory.getOrder()));

        // Добавим обработчики кликов по кнопкам
        Button btnSave = (Button)findViewById(R.id.btnSave);
        if (btnSave != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(onBtnSaveClick);
        }

        // Установим заголовок формы
        if (mIsNewCategory){
            setTitle(getString(R.string.new_category));
        }
        else{
            setTitle(getString(R.string.category_title));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(String.valueOf(R.string.category), mCategory);
        outState.putBoolean(String.valueOf(R.string.is_new_item), mIsNewCategory);

        super.onSaveInstanceState(outState);
    }

    private final View.OnClickListener onBtnSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText etCategoryName = (EditText) findViewById(R.id.etCategoryName);
            EditText etCategoryOrder = (EditText) findViewById(R.id.etOrder);
            if (etCategoryName != null && etCategoryOrder != null) {
                mCategory.setName(etCategoryName.getText().toString());
                mCategory.setOrder(Integer.parseInt(etCategoryOrder.getText().toString()));

                if (mIsNewCategory) {
                    mCategory.addToDB(getApplicationContext());
                } else {
                    mCategory.updateInDB(getApplicationContext());
                }
            }

            finish();
        }
    };
}


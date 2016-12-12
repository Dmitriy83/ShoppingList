package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.utils.Utils;
import com.squareup.picasso.Picasso;

public class CategoryActivity extends AppCompatActivity{

    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_editing);

        if (savedInstanceState != null){
            // Восстановим объект из сохраненных значений
            mCategory = savedInstanceState.getParcelable(String.valueOf(R.string.category));
        }else{
            // Получим значения из переданных параметров
            mCategory = getIntent().getParcelableExtra(String.valueOf(R.string.category));
        }

        if (mCategory == null) {
            mCategory = new Category(-1, "", 100, 0);
            mCategory.isNew = true;
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mCategory.isNew){
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

        ImageView imgCategory = (ImageView)findViewById(R.id.imgItemImage);
        if (imgCategory != null){
            imgCategory.setOnClickListener(onImgItemClick);
        }

        // Установим заголовок формы
        if (mCategory.isNew){
            setTitle(getString(R.string.new_category));
        }
        else{
            setTitle(getString(R.string.category_title));
        }

        setCategoryImage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(String.valueOf(R.string.category), mCategory);
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

                if (mCategory.isNew) {
                    mCategory.addToDB(getApplicationContext());
                } else {
                    mCategory.updateInDB(getApplicationContext());
                }
            }

            finish();
        }
    };

    private final View.OnClickListener onImgItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(), ChooseCategoryImageActivity.class);
            startActivityForResult(intent, Utils.GET_CATEGORY_IMAGE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Utils.GET_CATEGORY_IMAGE:
                if (resultCode == RESULT_OK) {
                    mCategory.setImageId(data.getIntExtra(getString(R.string.category_image_id), 0));
                    setCategoryImage();
                }
                break;
        }
    }

    private void setCategoryImage() {
        final ImageView imgCategory = (ImageView) findViewById(R.id.imgItemImage);
        if (imgCategory != null && mCategory != null) {
            int imageId = R.drawable.ic_default_product_image;
            if (mCategory.getCategoryImageId() != 0) imageId = mCategory.getCategoryImageId();
            // Установим картинку
            final int finalImageId = imageId; // Для использования в Callback
            Picasso.with(this)
                    .load(imageId)
                    .placeholder(R.drawable.ic_default_product_image)
                    .fit()
                    .into(imgCategory, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgCategory.setContentDescription(String.valueOf(finalImageId));
                        }

                        @Override
                        public void onError() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgCategory.setContentDescription(String.valueOf(R.drawable.ic_default_product_image));
                        }
                    });
        }
    }
}


package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;
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
            mCategory = savedInstanceState.getParcelable(EXTRAS_KEYS.CATEGORY.getValue());
        }else{
            // Получим значения из переданных параметров
            mCategory = getIntent().getParcelableExtra(EXTRAS_KEYS.CATEGORY.getValue());
        }

        if (mCategory == null) {
            mCategory = new Category(-1, "", 100);
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
        outState.putParcelable(EXTRAS_KEYS.CATEGORY.getValue(), mCategory);
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
                    String strImageUri = data.getStringExtra(EXTRAS_KEYS.ITEM_IMAGE.getValue());
                    if (strImageUri != null) {
                        mCategory.setImageUri(Uri.parse(strImageUri));
                    }
                    else{
                        mCategory.setImageUri(null);
                    }
                    setCategoryImage();
                }
                break;
        }
    }

    private void setCategoryImage() {
        final ImageView imgCategory = (ImageView) findViewById(R.id.imgItemImage);
        if (imgCategory != null && mCategory != null) {
            int imageResource = R.drawable.ic_default_product_image;
            if (mCategory.getImageUri() != null)
                imageResource = getResources().getIdentifier(
                    mCategory.getImageUri().toString(), null, getPackageName());
            final int finalImageResource = imageResource;
            Picasso.with(this)
                    .load(imageResource)
                    .placeholder(R.drawable.ic_default_product_image)
                    .fit()
                    .into(imgCategory, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgCategory.setContentDescription(String.valueOf(finalImageResource));
                        }

                        @Override
                        public void onError() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgCategory.setContentDescription(String.valueOf(R.drawable.ic_default_product_image));
                        }
                    });
            // Если mCategory.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (mCategory.getImageUri() == null) imgCategory.setContentDescription(
                    String.valueOf(R.drawable.ic_default_product_image));
        }
    }
}


package com.RightDirection.ShoppingList.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ShowItemImageActivity extends BaseActivity{
    private Uri mImageUri = null;
    private final String TAG = ShowItemImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_image);

        String strImageUri = getIntent().getStringExtra(EXTRAS_KEYS.ITEM_IMAGE.getValue());
        if (strImageUri != null){
            try {
                mImageUri = Uri.parse(strImageUri);
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
                mImageUri = null;
            }
        }

        // Установим заголовок формы
        String productName = getIntent().getStringExtra(EXTRAS_KEYS.PRODUCT_NAME.getValue());
        if (productName != null){
            try {
                setTitle(getString(R.string.product_image_activity_title, productName));
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }

        setProductImage();

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setProductImage() {
        final PhotoView imgProduct = (PhotoView) findViewById(R.id.imgItemImage);
        if (imgProduct != null) {
            // Установим картинку
            Picasso.with(this)
                    .load(mImageUri)
                    .placeholder(android.R.drawable.ic_menu_crop)
                    .fit().centerInside()
                    .into(imgProduct, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgProduct.setContentDescription(String.valueOf(mImageUri));
                        }

                        @Override
                        public void onError() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
                        }
                    });
            // Если mProduct.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (mImageUri == null) {
                imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Нажатие на стрелку (кнопка Up)
                onBackPressed();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }
}

package com.RightDirection.ShoppingList.activities;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

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
    }

    private void setProductImage() {
        final PhotoView imgProduct = findViewById(R.id.imgItemImage);
        if (imgProduct != null) {
            // Установим картинку
            Glide.with(this)
                    .load(mImageUri)
                    .listener(new RequestListener<Drawable>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                      // Для поиска элемента при тестировании запишем imageId в contentDescription
                                      imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                      // Для поиска элемента при тестировании запишем imageId в contentDescription
                                      imgProduct.setContentDescription(String.valueOf(mImageUri));
                                      return false;
                                  }
                              }
                    )
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_crop)
                            .centerInside()
                            .dontAnimate()
                            .dontTransform())
                    .into(imgProduct);
            // Если mProduct.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (mImageUri == null) {
                imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
            }
        }
    }
}

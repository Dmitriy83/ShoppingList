package com.RightDirection.ShoppingList.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class CategoryActivity extends BaseActivity{

    private Category mCategory;
    private final int DEFAULT_ORDER = 100;
    private static final int PICK_IMAGE = 7;
    public static final int RESULT_PICK_CUSTOM_IMAGE = 5;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

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
            mCategory = new Category(Utils.EMPTY_ID, "", DEFAULT_ORDER);
            mCategory.isNew = true;
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mCategory.isNew){
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etCategoryName = findViewById(R.id.etCategoryName);
        if (etCategoryName != null) etCategoryName.setText(mCategory.getName());

        EditText etCategoryOrder = findViewById(R.id.etOrder);
        if (etCategoryOrder != null) etCategoryOrder.setText(String.valueOf(mCategory.getOrder()));

        // Добавим обработчики кликов по кнопкам
        Button btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(onBtnSaveClick);
        }

        ImageView imgCategory = findViewById(R.id.imgItemImage);
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
            EditText etCategoryName = findViewById(R.id.etCategoryName);
            EditText etCategoryOrder = findViewById(R.id.etOrder);
            if (etCategoryName != null && etCategoryOrder != null) {
                mCategory.setName(etCategoryName.getText().toString());

                if (!etCategoryOrder.getText().toString().isEmpty()){
                    mCategory.setOrder(Integer.parseInt(etCategoryOrder.getText().toString()));
                }else {
                    mCategory.setOrder(DEFAULT_ORDER);
                }

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
                if (resultCode == RESULT_PICK_CUSTOM_IMAGE) {
                    chooseCategoryCustomImage();
                }
                break;
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    mCategory.setImageUri(data.getData());
                    askForPermissionAndSetCategoryImage();
                }
                break;
        }
    }

    private void setCategoryImage() {
        final ImageView imgCategory = findViewById(R.id.imgItemImage);
        if (imgCategory != null && mCategory != null) {
            int imageResource = R.drawable.ic_default_product_image;
            Uri imageUri = mCategory.getImageUri();
            boolean isInnerImage = true;
            String strImageResource = "";
            if (imageUri != null) {
                isInnerImage = imageUri.toString().contains("drawable/");
                if (isInnerImage) {
                    imageResource = getResources().getIdentifier(imageUri.toString(), null, getPackageName());
                    strImageResource = String.valueOf(imageResource);
                } else{
                    strImageResource = String.valueOf(imageUri);
                }
            }
            final String finalImageResource = strImageResource;
            Glide.with(this)
                    .load(isInnerImage ? imageResource : imageUri)
                    .listener(new RequestListener<Drawable>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                      // Для поиска элемента при тестировании запишем imageId в contentDescription
                                      imgCategory.setContentDescription(String.valueOf(R.drawable.ic_default_product_image));
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                      // Для поиска элемента при тестировании запишем imageId в contentDescription
                                      imgCategory.setContentDescription(finalImageResource);
                                      return false;
                                  }
                              }
                    )
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_crop)
                            .centerInside()
                            .dontAnimate()
                            .dontTransform())
                    .into(imgCategory);

            // Если mCategory.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (imageUri == null) imgCategory.setContentDescription(String.valueOf(R.drawable.ic_default_product_image));
        }
    }

    private void chooseCategoryCustomImage(){

        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(intent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);

    }

    private void askForPermissionAndSetCategoryImage() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                //noinspection StatementWithEmptyBody
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
            } else {
                setCategoryImage();
            }
        } else {
            setCategoryImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                //noinspection StatementWithEmptyBody
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    setCategoryImage();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}


package com.RightDirection.ShoppingList.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.squareup.picasso.Picasso;

public class ItemActivity extends AppCompatActivity{

    private boolean mIsNewItem;
    private Product mProduct;

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String KEY_PRODUCT = "PRODUCT";
    private static final String KEY_IS_NEW_ITEM = "IS_NEW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing);

        if (savedInstanceState != null){
            // Восстановим объект из сохраненных значений
            mProduct = savedInstanceState.getParcelable(KEY_PRODUCT);
            mIsNewItem = savedInstanceState.getBoolean(KEY_IS_NEW_ITEM);
        }else{
            // Получим значения из переданных параметров
            Intent sourceIntent = getIntent();
            mIsNewItem = sourceIntent.getBooleanExtra(String.valueOf(R.string.is_new_item), true);
            long id = sourceIntent.getLongExtra(String.valueOf(R.string.item_id), -1);
            Uri imageUri = sourceIntent.getParcelableExtra(String.valueOf(R.string.item_image));
            String name = sourceIntent.getStringExtra(String.valueOf(R.string.name));

            mProduct = new Product(id, name, imageUri);
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mIsNewItem){
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etProductName = (EditText) findViewById(R.id.etProductName);
        if (etProductName != null) {
            etProductName.setText(mProduct.getName());
        }

        // Добавим обработчики кликов по кнопкам
        Button btnSaveProduct = (Button)findViewById(R.id.btnSaveProduct);
        if (btnSaveProduct != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSaveProduct.setTransformationMethod(null);
            btnSaveProduct.setOnClickListener(onBtnSaveProductClick);
        }
        ImageView imgProduct = (ImageView)findViewById(R.id.imgProduct);
        if (imgProduct != null){
            imgProduct.setOnClickListener(onImgProductClick);
        }

        // Установим заголовок формы
        if (mIsNewItem){
            setTitle(getString(R.string.new_product));
        }
        else{
            setTitle(getString(R.string.product_title));
        }

        setProductImage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_PRODUCT, mProduct);
        outState.putBoolean(KEY_IS_NEW_ITEM, mIsNewItem);

        super.onSaveInstanceState(outState);
    }

    private final View.OnClickListener onBtnSaveProductClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText etProductName = (EditText) findViewById(R.id.etProductName);
            if (etProductName != null) {
                mProduct.setName(etProductName.getText().toString());

                if (mIsNewItem) {
                    mProduct.addToDB(getApplicationContext());
                } else {
                    mProduct.updateInDB(getApplicationContext());
                }
                Intent intent = new Intent();
                intent.putExtra(String.valueOf(R.string.item_id), mProduct.getId());
                intent.putExtra(String.valueOf(R.string.name), mProduct.getName());
                intent.putExtra(String.valueOf(R.string.item_image), mProduct.getImageUri());
                setResult(RESULT_OK, intent);
            }

            finish();
        }
    };

    private final View.OnClickListener onImgProductClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

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
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case PICK_IMAGE:
            if (resultCode == RESULT_OK) {
                mProduct.setImageUri(imageReturnedIntent.getData());
                askForPermissionAndSetProductImage();
            }
        }
    }

    private void askForPermissionAndSetProductImage(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
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
                setProductImage();
            }
        }
        else{
            setProductImage();
        }
    }

    private void setProductImage(){
        ImageView imgProduct = (ImageView) findViewById(R.id.imgProduct);
        if (imgProduct != null) {
            // Установим картинку
            Picasso.with(this)
                    .load(mProduct.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_crop)
                    .fit()
                    .into(imgProduct);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    setProductImage();
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


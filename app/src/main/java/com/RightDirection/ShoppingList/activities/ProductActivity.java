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
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.utils.DecimalDigitsInputFilter;
import com.RightDirection.ShoppingList.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ProductActivity extends BaseActivity {

    private Product mProduct;
    private static final int PICK_IMAGE = 1;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String TAG = ProductActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_editing);

        if (savedInstanceState != null) {
            // Восстановим объект из сохраненных значений
            mProduct = savedInstanceState.getParcelable(EXTRAS_KEYS.PRODUCT.getValue());
        } else {
            // Получим значения из переданных параметров
            mProduct = getIntent().getParcelableExtra(EXTRAS_KEYS.PRODUCT.getValue());
        }

        if (mProduct == null) {
            mProduct = new Product(Utils.EMPTY_ID);
            mProduct.isNew = true;
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mProduct.isNew) {
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etProductName = findViewById(R.id.etProductName);
        if (etProductName != null) {
            etProductName.setText(mProduct.getName());
        }

        Button btnChooseCategory = findViewById(R.id.btnChooseCategory);
        if (btnChooseCategory != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnChooseCategory.setTransformationMethod(null);
            btnChooseCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onBtnChooseCategoryClick(); }
            });
            setBtnChooseCategoryText();
        }

        Button btnSaveProduct = findViewById(R.id.btnSave);
        if (btnSaveProduct != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSaveProduct.setTransformationMethod(null);
            btnSaveProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onBtnSaveClick(); }
            });
        }
        ImageView imgProduct = findViewById(R.id.imgItemImage);
        if (imgProduct != null) {
            imgProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onImgProductClick(); }
            });
        }

        // Установим заголовок формы
        if (mProduct.isNew) {
            setTitle(getString(R.string.new_product));
        } else {
            setTitle(getString(R.string.product_title));
        }

        setProductImage();

        ImageButton ibShowImage = findViewById(R.id.ibShowImage);
        if (ibShowImage != null){
            ibShowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShowImageClick();
                }
            });
        }
        ImageButton ibClearImage = findViewById(R.id.ibClearImage);
        if (ibClearImage != null){
            ibClearImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClearImageClick();
                }
            });
        }

        ImageButton imgPriceChangeGraph = findViewById(R.id.imgPriceChangeGraph);
        if (imgPriceChangeGraph != null) {
            imgPriceChangeGraph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { onImgPriceChangeGraphClick(); }
            });
        }
        Button btnUnit = findViewById(R.id.btnUnit);
        if (btnUnit != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnUnit.setTransformationMethod(null);
            btnUnit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnUnitClick();
                }
            });
            setBtnUnitText();
        }
        // Ограничим ввод цены двумя знаками после запятой
        EditText etPrice = findViewById(R.id.etLastPrice);
        if (etPrice != null) {
            etPrice.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
            try {
                if (mProduct.getLastPrice() != 0) {
                    etPrice.setText(String.format(Locale.ENGLISH, "%.2f", mProduct.getLastPrice()));
                }
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void onBtnUnitClick() {
        Intent intent = new Intent(getBaseContext(), ChooseUnitActivity.class);
        startActivityForResult(intent, Utils.GET_UNIT);
    }

    private void onImgPriceChangeGraphClick() {
        Intent intent = new Intent(getApplicationContext(), PriceChangeHistoryActivity.class);
        intent.putExtra(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        startActivity(intent);
    }

    private void onClearImageClick() {
        mProduct.setImageUri(null);
        setProductImage();
    }

    private void onShowImageClick() {
        Uri imageUri = mProduct.getImageUri();
        if (imageUri == null) return;

        Intent intent = new Intent(getApplicationContext(), ShowItemImageActivity.class);
        intent.putExtra(EXTRAS_KEYS.ITEM_IMAGE.getValue(), imageUri.toString());
        EditText etProductName = findViewById(R.id.etProductName);
        if (etProductName != null) {
            intent.putExtra(EXTRAS_KEYS.PRODUCT_NAME.getValue(), etProductName.getText().toString());
        }
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        super.onSaveInstanceState(outState);
    }

    private void onBtnChooseCategoryClick() {
        Intent intent = new Intent(getBaseContext(), ChooseCategoryActivity.class);
        startActivityForResult(intent, Utils.GET_CATEGORY);
    }

    private void onBtnSaveClick() {
        EditText etProductName = findViewById(R.id.etProductName);
        if (etProductName != null) mProduct.setName(etProductName.getText().toString());

        EditText etLastPrice = findViewById(R.id.etLastPrice);
        try {
            if (etLastPrice != null) mProduct.setLastPrice(Float.valueOf(etLastPrice.getText().toString()));
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        if (mProduct.isNew) {
            mProduct.addToDB(getApplicationContext());
        } else {
            mProduct.updateInDB(getApplicationContext());
        }

        // Обновим наименование в активности редактирования списка товаров
        Intent intent = new Intent();
        intent.putExtra(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        setResult(RESULT_OK, intent);

        finish();
    }

    private void onImgProductClick() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    mProduct.setImageUri(data.getData());
                    askForPermissionAndSetProductImage();
                }
                break;
            case Utils.GET_CATEGORY:
                if (resultCode == RESULT_OK) {
                    mProduct.setCategory((Category) data.getParcelableExtra(EXTRAS_KEYS.CATEGORY.getValue()));
                    setBtnChooseCategoryText();
                }
                break;
            case Utils.GET_UNIT:
                if (resultCode == RESULT_OK) {
                    mProduct.setDefaultUnit((Unit) data.getParcelableExtra(EXTRAS_KEYS.UNIT.getValue()));
                    setBtnUnitText();
                }
                break;
        }
    }

    private void setBtnUnitText() {
        Unit unit = mProduct.getDefaultUnit();
        Button btnUnit = findViewById(R.id.btnUnit);
        if (btnUnit != null && unit != null && unit.getShortName() != null) {
            btnUnit.setText(unit.getShortName());
        }
    }

    private void setBtnChooseCategoryText() {
        Category category = mProduct.getCategory();
        Button btnChooseCategory = findViewById(R.id.btnChooseCategory);
        if (btnChooseCategory != null && category != null && category.getName() != null) {
            btnChooseCategory.setText(getString(R.string.three_dots, category.getName()));
        }
    }

    private void askForPermissionAndSetProductImage() {
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
                setProductImage();
            }
        } else {
            setProductImage();
        }
    }

    private void setProductImage() {
        final ImageView imgProduct = findViewById(R.id.imgItemImage);
        if (imgProduct != null && mProduct != null) {
            // Установим картинку
            Picasso.with(this)
                    .load(mProduct.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_crop)
                    .fit()
                    .into(imgProduct, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgProduct.setContentDescription(String.valueOf(mProduct.getImageUri()));
                            // Посде загрузки картинки отобразим кнопки "Посмотреть картинку" и "Очистить картинку"
                            showImageButtons();
                        }

                        @Override
                        public void onError() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
                            // Если произошла ошибка при загрузке картинки, то нужно скрыть кнопки "Посмотреть картинку" и "Очистить картинку"
                            clearImageButtons();
                        }
                    });
            // Если mProduct.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (mProduct.getImageUri() == null) {
                imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
                // Если картинка по какой-то причине не загрузилась, нужно скрыть кнопки "Посмотреть картинку" и "Очистить картинку"
                clearImageButtons();
            }
        }
    }

    private void showImageButtons(){
        ImageButton ibShowImage = findViewById(R.id.ibShowImage);
        if (ibShowImage != null){
            ibShowImage.setVisibility(View.VISIBLE);
        }
        ImageButton ibClearImage = findViewById(R.id.ibClearImage);
        if (ibClearImage != null){
            ibClearImage.setVisibility(View.VISIBLE);
        }
    }

    private void clearImageButtons(){
        ImageButton ibShowImage = findViewById(R.id.ibShowImage);
        if (ibShowImage != null){
            ibShowImage.setVisibility(View.GONE);
        }
        ImageButton ibClearImage = findViewById(R.id.ibClearImage);
        if (ibClearImage != null){
            ibClearImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                //noinspection StatementWithEmptyBody
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


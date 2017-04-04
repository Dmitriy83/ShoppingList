package com.RightDirection.ShoppingList.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.utils.Utils;
import com.squareup.picasso.Picasso;

public class ProductActivity extends BaseActivity {

    private Product mProduct;

    private static final int PICK_IMAGE = 1;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

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
            mProduct = new Product(-1);
            mProduct.isNew = true;
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mProduct.isNew) {
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etProductName = (EditText) findViewById(R.id.etProductName);
        if (etProductName != null) {
            etProductName.setText(mProduct.getName());
        }

        Button btnChooseCategory = (Button) findViewById(R.id.btnChooseCategory);
        if (btnChooseCategory != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnChooseCategory.setTransformationMethod(null);

            Category category = mProduct.getCategory();
            if (category != null && category.getName() != null)
                btnChooseCategory.setText(getString(R.string.three_dots, category.getName()));

            // Обработчик нажатия
            btnChooseCategory.setOnClickListener(onBtnChooseCategoryClick);
        }

        Button btnSaveProduct = (Button) findViewById(R.id.btnSave);
        if (btnSaveProduct != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSaveProduct.setTransformationMethod(null);
            btnSaveProduct.setOnClickListener(onBtnSaveClick);
        }
        ImageView imgProduct = (ImageView) findViewById(R.id.imgItemImage);
        if (imgProduct != null) {
            imgProduct.setOnClickListener(onImgProductClick);
        }

        // Установим заголовок формы
        if (mProduct.isNew) {
            setTitle(getString(R.string.new_product));
        } else {
            setTitle(getString(R.string.product_title));
        }

        setProductImage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        super.onSaveInstanceState(outState);
    }

    private final View.OnClickListener onBtnChooseCategoryClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(), ChooseCategoryActivity.class);
            startActivityForResult(intent, Utils.GET_CATEGORY);
        }
    };

    private final View.OnClickListener onBtnSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText etProductName = (EditText) findViewById(R.id.etProductName);
            if (etProductName != null) mProduct.setName(etProductName.getText().toString());

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
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        }
    };

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
                    Category category = data.getParcelableExtra(getString(R.string.category));
                    mProduct.setCategory(category);
                    Button btnChooseCategory = (Button) findViewById(R.id.btnChooseCategory);
                    if (btnChooseCategory != null && category != null && category.getName() != null) {
                        btnChooseCategory.setText(getString(R.string.three_dots, category.getName()));
                    }
                }
                break;
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
        final ImageView imgProduct = (ImageView) findViewById(R.id.imgItemImage);
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
                        }

                        @Override
                        public void onError() {
                            // Для поиска элемента при тестировании запишем imageId в contentDescription
                            imgProduct.setContentDescription(String.valueOf(android.R.drawable.ic_menu_crop));
                        }
                    });
            // Если mProduct.getImageUri() == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
            if (mProduct.getImageUri() == null) imgProduct.setContentDescription(
                    String.valueOf(android.R.drawable.ic_menu_crop));
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


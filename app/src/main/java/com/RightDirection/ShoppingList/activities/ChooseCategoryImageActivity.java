package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.GridAdapterChooseCategoryImage;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;

public class ChooseCategoryImageActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_category_image);

        RecyclerView recyclerView = findViewById(R.id.rvCategoryImages);
        if (recyclerView == null) return;

        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        // Создадим новый адаптер для работы со списком товаров
        GridAdapterChooseCategoryImage gridAdapter = new GridAdapterChooseCategoryImage(this);

        // Привяжем адаптер к элементу управления
        recyclerView.setAdapter(gridAdapter);

        Button btnCategoryEmpty = findViewById(R.id.btnCategoryEmpty);
        if (btnCategoryEmpty != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnCategoryEmpty.setTransformationMethod(null);
            btnCategoryEmpty.setOnClickListener(onBtnCategoryEmptyClick);
        }
    }

    private final View.OnClickListener onBtnCategoryEmptyClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendImageUriAndFinish(null);
        }
    };

    public void sendImageUriAndFinish(String strImageUri){
        Intent intent = new Intent();
        intent.putExtra(EXTRAS_KEYS.ITEM_IMAGE.getValue(), strImageUri);
        setResult(RESULT_OK, intent);

        finish();
    }
}


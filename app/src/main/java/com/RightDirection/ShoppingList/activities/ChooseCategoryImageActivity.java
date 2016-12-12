package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.GridAdapterChooseCategoryImage;

public class ChooseCategoryImageActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_category_image);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rvCategoryImages);
        if (recyclerView == null) return;

        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        // Создадим новый адаптер для работы со списком товаров
        GridAdapterChooseCategoryImage gridAdapter = new GridAdapterChooseCategoryImage(this);

        // Привяжем адаптер к элементу управления
        recyclerView.setAdapter(gridAdapter);

        Button btnCategoryEmpty = (Button)findViewById(R.id.btnCategoryEmpty);
        if (btnCategoryEmpty != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnCategoryEmpty.setTransformationMethod(null);
            btnCategoryEmpty.setOnClickListener(onBtnCategoryEmptyClick);
        }
    }

    private final View.OnClickListener onBtnCategoryEmptyClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendImageIdAndFinish(0);
        }
    };

    public void sendImageIdAndFinish(int id){
        Intent intent = new Intent();
        intent.putExtra(getString(R.string.category_image_id), id);
        setResult(RESULT_OK, intent);

        finish();
    }
}

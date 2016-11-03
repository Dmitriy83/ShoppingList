package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.CategoryActivity;
import com.RightDirection.ShoppingList.items.Category;

import java.util.ArrayList;

public class ListAdapterCategoriesList extends ListAdapter {

    public ListAdapterCategoriesList(Context context, int resource, ArrayList<Category> objects) {
        super(context, resource, objects);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        if (viewHolder.represent != null)
            viewHolder.represent.setOnClickListener(onRepresentClick);
        if (viewHolder.imgDelete != null)
            viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
    }

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            Category item = (Category) view.getTag();
            // Удалим из БД
            item.removeFromDB(mParentActivity);
            // Удалим из списка
            remove(item);
        }
    };

    private final View.OnClickListener onRepresentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Category item = (Category) view.getTag();

            // Откроем активность редактирования продукта
            Intent intent = new Intent(mParentActivity.getBaseContext(), CategoryActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_item), false);
            intent.putExtra(String.valueOf(R.string.category), item);
            mParentActivity.startActivity(intent);
        }
    };
}
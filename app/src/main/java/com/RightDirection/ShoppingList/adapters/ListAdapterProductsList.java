package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ProductActivity;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.utils.Utils;

import java.util.ArrayList;

public class ListAdapterProductsList extends ListAdapter {

    public ListAdapterProductsList(Context context, int resource, ArrayList<Product> objects) {
        super(context, resource, objects);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        if (viewHolder.represent != null)
            viewHolder.represent.setOnClickListener(onProductRepresentClick);
        if (viewHolder.imgDelete != null)
            viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
    }

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            Product item = (Product) view.getTag();
            // Удалим из БД
            item.removeFromDB(mParentActivity);
            // Удалим из списка
            remove(item);
        }
    };

    private final View.OnClickListener onProductRepresentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Product product = (Product) view.getTag();

            // Откроем активность редактирования продукта
            Intent intent = new Intent(mParentActivity.getBaseContext(), ProductActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_item), false);
            intent.putExtra(String.valueOf(R.string.product), product);
            intent.putExtra(String.valueOf(R.string.category), product.getCategory());
            mParentActivity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
        }
    };
}
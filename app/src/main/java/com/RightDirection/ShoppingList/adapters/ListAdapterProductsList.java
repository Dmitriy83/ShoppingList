package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.items.Product;

import java.util.ArrayList;

public class ListAdapterProductsList extends ListAdapter {

    public ListAdapterProductsList(Context context, ArrayList<IListItem> objects) {
        super(context, com.RightDirection.ShoppingList.R.layout.list_item_products_list, objects);
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
            product.startProductActivity(mParentActivity);
        }
    };
}
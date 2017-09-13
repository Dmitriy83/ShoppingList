package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;

import java.util.ArrayList;

public class ListAdapterCategoriesList extends BaseListAdapter {

    public ListAdapterCategoriesList(Context context, ArrayList<IListItem> objects) {
        super(context, com.RightDirection.ShoppingList.R.layout.list_item_categories_list, objects);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        if (viewHolder.represent != null)
            viewHolder.represent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onRepresentClick(view); }
            });
        if (viewHolder.imgDelete != null)
            viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onImageDeleteClick(view); }
            });
        if (viewHolder.itemImage != null) {
            setProductImage(viewHolder.itemImage, mObjects.get(position));
        }
    }

    private void onImageDeleteClick(View view) {
        // Получим объект item по элементу View
        Category item = (Category) view.getTag();
        // Удалим из БД
        item.removeFromDB(mParentActivity);
        // Удалим из списка
        remove(item);
    }

    private void onRepresentClick(View view) {
        Category category = (Category) view.getTag();
        category.startCategoryActivity(mParentActivity);
    }
}

package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.Category;

import java.util.ArrayList;

public class ListAdapterCategoriesList extends ListAdapter {

    public ListAdapterCategoriesList(Context context, int resource, ArrayList<Category> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewInitializer viewInitializer = new ViewInitializer(position, convertView);

        if (viewInitializer.viewHolder != null) {
            if (viewInitializer.viewHolder.represent != null)
                viewInitializer.viewHolder.represent.setOnClickListener(onRepresentClick);
            if (viewInitializer.viewHolder.imgDelete != null)
                viewInitializer.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        }

        return viewInitializer.rowView;
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
            notifyDataSetChanged();
        }
    };

    private final View.OnClickListener onRepresentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Category item = (Category) view.getTag();

            // TODO: Переименование категории


        }
    };
}

package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ItemActivity;

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
            Product item = (Product) view.getTag();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mParentActivity);
            boolean showImages = sharedPref.getBoolean(mParentActivity.getString(R.string.pref_key_show_images), true);

            // Откроем активность редактирования продукта
            Intent intent = new Intent(mParentActivity.getBaseContext(), ItemActivity.class);
            intent.putExtra(String.valueOf(R.string.name), item.getName());
            intent.putExtra(String.valueOf(R.string.item_id), item.getId());
            intent.putExtra(String.valueOf(R.string.item_image), item.getImageUri());
            intent.putExtra(String.valueOf(R.string.is_new_item), false);
            mParentActivity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
        }
    };
}
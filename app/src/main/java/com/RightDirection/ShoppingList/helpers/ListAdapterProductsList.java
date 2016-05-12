package com.RightDirection.ShoppingList.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ItemActivity;

import java.util.List;

public class ListAdapterProductsList extends ListAdapter {

    public ListAdapterProductsList(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Parameters parameters = new Parameters(position, convertView);

        parameters.viewHolder.productNameView.setOnClickListener(onProductNameViewClick);
        parameters.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        // Привяжем к View объект ListItem
        parameters.viewHolder.imgDelete.setTag(parameters.item);

        return parameters.rowView;
    }

    protected static class ViewHolder {
        public TextView productName;
    }

    private View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            ListItem item = (ListItem) view.getTag();

            // Удалим запись из БД по id
            // 1) Удаление из справочника продуктов
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                        ShoppingListContentProvider.KEY_ID + "=" + item.getId(), null);
            // 2) Удаление ссылок на удаленный продукт
            contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                    ShoppingListContentProvider.KEY_PRODUCT_ID + "=" + item.getId(), null);

            remove(item);
            notifyDataSetChanged();
        }
    };

    private View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItem item = (ListItem) view.getTag();

            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
            if (cursor.moveToFirst()) {
                // Откроем окно редактирования элемента списка продуктов
                String productName = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_NAME));
                String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                Intent intent = new Intent(mParentActivity.getBaseContext(), ItemActivity.class);
                intent.putExtra(String.valueOf(R.string.name), productName);
                intent.putExtra(String.valueOf(R.string.item_id), itemId);
                intent.putExtra(String.valueOf(R.string.is_new_item), false);
                mParentActivity.startActivity(intent);
            }
            cursor.close();
        }
    };
}

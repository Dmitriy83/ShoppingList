package com.RightDirection.ShoppingList.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ItemActivity;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterProductsList extends ListAdapter {

    public ListAdapterProductsList(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GetViewInitializer getViewInitializer = new GetViewInitializer(position, convertView);

        if (getViewInitializer.viewHolder != null) {
            if (getViewInitializer.viewHolder.productRepresent != null)
                getViewInitializer.viewHolder.productRepresent.setOnClickListener(onProductRepresentClick);
            if (getViewInitializer.viewHolder.imgDelete != null)
                getViewInitializer.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        }

        return getViewInitializer.rowView;
    }

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            ListItem item = (ListItem) view.getTag();

            // Удалим запись из БД по id
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                        ShoppingListContentProvider.KEY_ID + "=" + item.getId(), null);

            remove(item);
            notifyDataSetChanged();
        }
    };

    private final View.OnClickListener onProductRepresentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItem item = (ListItem) view.getTag();

            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    // Откроем окно редактирования элемента списка продуктов
                    String productName = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_NAME));
                    String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                    String itemImageUri = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_PICTURE));
                    Intent intent = new Intent(mParentActivity.getBaseContext(), ItemActivity.class);
                    intent.putExtra(String.valueOf(R.string.name), productName);
                    intent.putExtra(String.valueOf(R.string.item_id), itemId);
                    intent.putExtra(String.valueOf(R.string.item_image), itemImageUri);
                    intent.putExtra(String.valueOf(R.string.is_new_item), false);
                    mParentActivity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
                }
                cursor.close();
            }
        }
    };
}

package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ActionsSubmenuActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterMainActivity extends ListAdapter {

    public ListAdapterMainActivity(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GetViewInitializer getViewInitializer = new GetViewInitializer(position, convertView);

        getViewInitializer.viewHolder.productNameView.setOnClickListener(onProductNameViewClick);
        getViewInitializer.viewHolder.productNameView.setOnLongClickListener(onProductNameViewLongClick);
        getViewInitializer.viewHolder.imgActions.setOnClickListener(onImgActionsClick);
        // Привяжем к View объект ListItem
        getViewInitializer.viewHolder.imgActions.setTag(getViewInitializer.item);

        return getViewInitializer.rowView;
    }

    private final View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItem item = (ListItem) view.getTag();
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                    null, "_id = " + item.getId(), null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Activity parentActivity = (Activity) mContext;
                    Intent intent = new Intent(parentActivity, ShoppingListInShopActivity.class);
                    String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                    intent.putExtra(String.valueOf(R.string.list_id), itemId);
                    ActivityCompat.startActivity(parentActivity, intent, null);
                }
                cursor.close();
            }
        }
    };

    private final View.OnLongClickListener onProductNameViewLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // Откроем список для редактирования
            ListItem item = (ListItem) v.getTag();
            Intent intent = new Intent(mParentActivity.getBaseContext(), ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_list), false);
            intent.putExtra(String.valueOf(R.string.list_id), item.getId());
            mParentActivity.startActivity(intent);

            return true;
        }
    };

    private final View.OnClickListener onImgActionsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Получим объект item по элементу View
            ListItem item = (ListItem) view.getTag();

            // Определим координаты кнопки
            int[] location = {0, 0};
            view.getLocationInWindow(location);

            // Отобрази подменю выбора действия
            try {
                ActionsSubmenuActivity.mCallingActivityAdapter = mListAdapter;

                notifyDataSetChanged();
                Intent intent = new Intent(mParentActivity, ActionsSubmenuActivity.class);
                intent.putExtra("y", location[1]);
                intent.putExtra(String.valueOf(R.string.list_item), item);
                mParentActivity.startActivity(intent);
            }
            finally{
                // Ничего не делаем
            }
        }
    };
}

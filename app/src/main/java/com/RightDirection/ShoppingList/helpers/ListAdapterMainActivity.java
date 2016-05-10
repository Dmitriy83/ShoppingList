package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ActionsSubmenuActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;

import java.util.List;

public class ListAdapterMainActivity extends ListAdapter {

    public ListAdapterMainActivity(Context context, int resource, List<ListItem> objects, FragmentManager fragmentManager) {
        super(context, resource, objects, fragmentManager);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout listView;

        ListItem item = getItem(position);

        String name = item.getName();

        if (convertView == null){
            listView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(mResource, listView, true);
        }
        else{
            listView = (LinearLayout)convertView;
        }

        TextView productNameView = (TextView)listView.findViewById(R.id.itemName);
        productNameView.setText(name);
        productNameView.setOnClickListener(onProductNameViewClick);
        productNameView.setOnLongClickListener(onProductNameViewLongClick);
        // Добавим сопоставление элемента управления и id элемента списка
        mViewAndIdMatcher.put(productNameView, item);

        ImageView imgActions = (ImageView) listView.findViewById(R.id.imgActions);
        imgActions.setOnClickListener(onImgActionsClick);
        // Добавим сопоставление элемента управления и id элемента списка
        mViewAndIdMatcher.put(imgActions, item);

        return listView;
    }

    private View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItem item = (ListItem) mViewAndIdMatcher.get(view);
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                    null, "_id = " + item.getId(), null, null);
            if (cursor.moveToFirst()) {
                Activity parentActivity = (Activity)mContext;
                Intent intent = new Intent(parentActivity, ShoppingListInShopActivity.class);
                String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                intent.putExtra(String.valueOf(R.string.list_id), itemId);
                ActivityCompat.startActivity(parentActivity, intent, null);
            }
            cursor.close();
        }
    };

    private View.OnLongClickListener onProductNameViewLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // Откроем список для редактирования
            ListItem item = (ListItem) mViewAndIdMatcher.get(v);
            ContentResolver contentResolver = mParentActivity.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                    null, "_id = " + item.getId(), null, null);
            if (cursor.moveToFirst()) {
                Intent intent = new Intent(mParentActivity.getBaseContext(), ShoppingListEditingActivity.class);
                intent.putExtra(String.valueOf(R.string.is_new_list), false);
                String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                intent.putExtra(String.valueOf(R.string.list_id), itemId);
                mParentActivity.startActivity(intent);
            }
            cursor.close();
            return true;
        }
    };

    private View.OnClickListener onImgActionsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Получим объект item по элементу View
            ListItem item = (ListItem)mViewAndIdMatcher.get(view);

            // Определим координаты кнопки
            int[] location = {0, 0};
            view.getLocationInWindow(location);

            // Отобрази подменю выбора действия
            try {
                ActionsSubmenuActivity.mCallingActivityAdapter = mListAdapter;
                ActionsSubmenuActivity.mListItem = item;

                notifyDataSetChanged();
                Intent intent = new Intent(mParentActivity, ActionsSubmenuActivity.class);
                intent.putExtra("y", location[1]);
                mParentActivity.startActivity(intent);
            }
            finally{
                // Ничего не делаем
            }
        }
    };
}

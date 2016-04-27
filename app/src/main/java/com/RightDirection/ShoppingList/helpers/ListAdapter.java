package com.RightDirection.ShoppingList.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.MainActivity;
import com.RightDirection.ShoppingList.activities.ProductsListActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListActivity;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;
import com.RightDirection.ShoppingList.ListItem;

import java.util.LinkedHashMap;
import java.util.List;

public class ListAdapter extends ArrayAdapter<ListItem> {

    int mResource;
    Context mContext;
    LinkedHashMap mViewAndIdMatcher;

    public ListAdapter(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mViewAndIdMatcher = new LinkedHashMap();

        // Проверим поддерживают ли вызвавшие активности требуемые интерфейсы
        checkRequiredInterfaces(context);
    }

    private void checkRequiredInterfaces(Context context) {
        try {
            IOnDeleteItemListener iOnDeleteItemListener = (IOnDeleteItemListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " должна поддерживать итерфейс IOnDeleteItemListener");
        }
        try {
            IOnClickItemListener iOnClickItemListener = (IOnClickItemListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " должна поддерживать итерфейс IOnClickItemListener");
        }
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

        ImageView imgDelete = (ImageView) listView.findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(onImgDeleteClick);

        // Добавим сопоставление элемента управления и id элемента списка
        mViewAndIdMatcher.put(imgDelete, item);
        mViewAndIdMatcher.put(productNameView, item);

        return listView;
    }

    private View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим Id по элементу View
            ListItem item = (ListItem)mViewAndIdMatcher.get(view);

            if(mContext instanceof ProductsListActivity){
                // Удалим запись из БД по id
                ContentResolver contentResolver = mContext.getContentResolver();
                contentResolver.delete(ShoppingListContentProvider.PRODUCTS_CONTENT_URI,
                        ShoppingListContentProvider.KEY_ID + "=" + item.getId(), null);

                // Сообщим связанному классу об событии
                ((IOnDeleteItemListener) mContext).onDeleteItem(null);
            }
            else {
                // Сообщим связанному классу об событии
                ((IOnDeleteItemListener) mContext).onDeleteItem(item);
            }
        }
    };

    private View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(mContext instanceof ProductsListActivity) {
                ListItem item = (ListItem) mViewAndIdMatcher.get(view);

                ContentResolver contentResolver = mContext.getContentResolver();
                Cursor cursor = contentResolver.query(ShoppingListContentProvider.PRODUCTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
                if (cursor.moveToFirst()) {
                    ((IOnClickItemListener) mContext).OnClickItem(cursor);
                }
            }
            else if(mContext instanceof MainActivity) {
                ListItem item = (ListItem) mViewAndIdMatcher.get(view);

                ContentResolver contentResolver = mContext.getContentResolver();
                Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
                if (cursor.moveToFirst()) {
                    ((IOnClickItemListener) mContext).OnClickItem(cursor);
                }
            }
            else {
                // Сообщим связанному классу об событии
                ((IOnClickItemListener) mContext).OnClickItem(null);
            }
        }
    };

}

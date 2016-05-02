package com.RightDirection.ShoppingList.helpers;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnEditItemListener;

import java.util.List;

public class ListAdapterMainActivity extends ListAdapter {

    public ListAdapterMainActivity(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);

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
        try {
            IOnEditItemListener iOnEditItemListener = (IOnEditItemListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " должна поддерживать итерфейс IOnEditItemListener");
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
        productNameView.setOnLongClickListener(onProductNameViewLongClick);

        ImageView imgEdit = (ImageView) listView.findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(onImgEditClick);

        // Добавим сопоставление элемента управления и id элемента списка
        mViewAndIdMatcher.put(productNameView, item);
        mViewAndIdMatcher.put(imgEdit, item);

        return listView;
    }

    private View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ListItem item = (ListItem) mViewAndIdMatcher.get(view);
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
            if (cursor.moveToFirst()) {
                ((IOnClickItemListener) mContext).OnClickItem(cursor);
            }
        }
    };

    private View.OnLongClickListener onProductNameViewLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // Получим объект item по элементу View
            ListItem item = (ListItem)mViewAndIdMatcher.get(v);
            // Сообщим связанному классу об событии
            ((IOnDeleteItemListener) mContext).onDeleteItem(item);

            return true;
        }
    };

    private View.OnClickListener onImgEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            ListItem item = (ListItem)mViewAndIdMatcher.get(view);

            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI, null, "_id = " + item.getId(), null, null);
            if (cursor.moveToFirst()) {
                ((IOnEditItemListener) mContext).OnEditItem(cursor);
            }
        }
    };
}

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.MainActivity;
import com.RightDirection.ShoppingList.activities.ProductsListActivity;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.interfaces.IOnDeleteItemListener;

import java.util.LinkedHashMap;
import java.util.List;

public class ListAdapterShoppingList extends ListAdapter {

    public ListAdapterShoppingList(Context context, int resource, List<ListItem> objects) {
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
            // Получим объект item по элементу View
            ListItem item = (ListItem)mViewAndIdMatcher.get(view);
            // Сообщим связанному классу об событии
            ((IOnDeleteItemListener) mContext).onDeleteItem(item);
        }
    };
}

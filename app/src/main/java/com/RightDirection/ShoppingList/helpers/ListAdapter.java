package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.views.SoftKeyboardListenedRelativeLayout;

import java.util.List;

abstract public class ListAdapter extends ArrayAdapter<ListItem>{

    int mResource;
    Context mContext;
    FragmentManager mFragmentManager = null;
    Activity mParentActivity;
    ListAdapter mListAdapter; // для доступа из обработичиков событий

    public ListAdapter(Context context, int resource, List<ListItem> objects, FragmentManager fragmentManager) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mFragmentManager = fragmentManager;
        mParentActivity = (Activity)context;
        mListAdapter = this;
    }

    public ListAdapter(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mParentActivity = (Activity)context;
        mListAdapter = this;
    }

    static class ViewHolder {
        public TextView productNameView;
        public ImageButton imgActions;
        public ImageButton imgDelete;
        public SoftKeyboardListenedRelativeLayout parentRelativeLayout;
    }

    /**
     * Класс-структура для получения и передачи параметров (item, viewHolder, rowView)
     */
    class Parameters{

        public ListItem item;
        public ViewHolder viewHolder;
        public LinearLayout rowView;

        /**
         * Конструктор
         * @param position позиция элемента в списке
         * @param convertView View-контейнер
         */
        Parameters(int position, View convertView){
            item = getItem(position);
            String name = item.getName();

            rowView = (LinearLayout)convertView;
            if (rowView == null){
                rowView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
                layoutInflater.inflate(mResource, rowView, true);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.productNameView = (TextView) rowView.findViewById(R.id.itemName);
                viewHolder.imgActions = (ImageButton) rowView.findViewById(R.id.imgActions);
                viewHolder.imgDelete = (ImageButton) rowView.findViewById(R.id.imgDelete);
                rowView.setTag(viewHolder);
            }
            else{
                viewHolder = (ViewHolder) rowView.getTag();
            }

            // fill data
            viewHolder.productNameView.setText(name);
            // Привяжем к View объект ListItem
            viewHolder.productNameView.setTag(item);
        }

    }


}

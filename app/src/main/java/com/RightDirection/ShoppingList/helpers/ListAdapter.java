package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

abstract public class ListAdapter extends ArrayAdapter<ListItem>{

    private final int mResource;
    final Context mContext;
    final Activity mParentActivity;
    final ListAdapter mListAdapter; // для доступа из обработичиков событий
    public ArrayList<ListItem> mObjects;

    ListAdapter(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mParentActivity = (Activity)context;
        mListAdapter = this;
        mObjects = objects;
    }

    static class ViewHolder {
        public TextView productNameView;
        public ImageButton imgDelete;
        public ImageView productImage;
        public RelativeLayout productRepresent;

        public ImageButton imgDecrease;
        public ImageButton imgIncrease;
        public EditText etCount;
        public TextView txtCount;
    }

    public void updateItem(long id, String name, Uri imageUri) {
        for (ListItem item: mObjects) {
            if (item.getId() == id){
                item.setName(name);
                item.setImageUri(imageUri);
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    /**
     * Класс-структура для получения и передачи параметров (item, viewHolder, rowView)
     */
    class ViewInitializer {

        public final ListItem item;
        public final ViewHolder viewHolder;
        public LinearLayout rowView;

        /**
         * Конструктор
         * @param position позиция элемента в списке
         * @param convertView View-контейнер
         */
        ViewInitializer(int position, View convertView){
            item = getItem(position);
            String name = item.getName();
            Float count = item.getCount();

            // Убедимся, что метод getView не вызывается лишнее количество раз
            Log.d("GET_VIEW_CALLING", "Position " + position + ", convertView " + convertView);

            rowView = (LinearLayout)convertView;
            if (rowView == null){
                rowView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
                layoutInflater.inflate(mResource, rowView, true);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.productNameView = (TextView) rowView.findViewById(R.id.txtName);
                viewHolder.imgDelete = (ImageButton) rowView.findViewById(R.id.imgDelete);
                viewHolder.productImage = (ImageView) rowView.findViewById(R.id.imgProduct);
                viewHolder.productRepresent = (RelativeLayout) rowView.findViewById(R.id.productRepresent);
                viewHolder.etCount = (EditText) rowView.findViewById(R.id.etCount);
                viewHolder.txtCount = (TextView) rowView.findViewById(R.id.txtCount);
                rowView.setTag(viewHolder);
            }
            else{
                viewHolder = (ViewHolder) rowView.getTag();
            }

            // Привяжем к View объект ListItem
            if (viewHolder.productNameView != null){
                viewHolder.productNameView.setTag(item);
                // Заполним текстовое поле
                viewHolder.productNameView.setText(name);
            }
            if (viewHolder.productRepresent != null) {
                viewHolder.productRepresent.setTag(item);
                viewHolder.productRepresent.setTag(R.string.view_holder, viewHolder);
            }
            if (viewHolder.imgDelete != null) viewHolder.imgDelete.setTag(item);

            Uri imageUri = item.getImageUri();
            if (viewHolder.productImage != null) {
                setProductImage(viewHolder.productImage, imageUri);
            }

            if (viewHolder.etCount != null){
                viewHolder.etCount.setTag(item);
                viewHolder.etCount.setText(String.format(Locale.ENGLISH, "%.1f", count));
            }
            if (viewHolder.txtCount != null){
                viewHolder.txtCount.setTag(item);
                viewHolder.txtCount.setText(String.format(Locale.ENGLISH, "%.1f", count));
            }
        }
    }

    private void setProductImage(ImageView imgProduct, Uri imageUri){
        if (imgProduct != null) {
            // Установим картинку
            Picasso.with(mParentActivity)
                    .load(imageUri)
                    .placeholder(android.R.drawable.presence_online)
                    .fit()
                    .into(imgProduct);
        }
    }
}

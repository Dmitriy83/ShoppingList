package com.RightDirection.ShoppingList.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.items.ListItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

abstract public class ListAdapter extends RecyclerView.Adapter{

    final int mResource;
    ArrayList mObjects;
    Activity mParentActivity;

    ListAdapter(Context context, int resource, ArrayList objects) {
        super();

        mResource = resource;
        mObjects = objects;
        mParentActivity = (Activity)context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(mResource, parent, false);
        return new ViewHolder(rowView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        ListItem item = (ListItem)mObjects.get(position);
        if (viewHolder.productNameView != null){
            viewHolder.productNameView.setTag(item);
            // Заполним текстовое поле
            viewHolder.productNameView.setText(item.getName());
        }
        if (viewHolder.represent != null) {
            viewHolder.represent.setTag(item);
            viewHolder.represent.setTag(R.string.view_holder, viewHolder);
        }
        if (viewHolder.imgDelete != null) {
            viewHolder.imgDelete.setTag(item);
        }

        Uri imageUri = item.getImageUri();
        if (viewHolder.productImage != null) {
            setProductImage(viewHolder.productImage, imageUri);
        }

        if (viewHolder.etCount != null){
            viewHolder.etCount.setTag(item);
            viewHolder.etCount.setText(String.format(Locale.ENGLISH, "%.1f", item.getCount()));
        }
        if (viewHolder.txtCount != null){
            viewHolder.txtCount.setTag(item);
            viewHolder.txtCount.setText(String.format(Locale.ENGLISH, "%.1f", item.getCount()));
        }
        if (viewHolder.imgDecrease != null) {
            viewHolder.imgDecrease.setTag(R.id.item, item);
            viewHolder.imgDecrease.setTag(R.id.etCount, viewHolder.etCount);
        }
        if (viewHolder.imgIncrease != null) {
            viewHolder.imgIncrease.setTag(R.id.item, item);
            viewHolder.imgIncrease.setTag(R.id.etCount, viewHolder.etCount);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameView;
        ImageButton imgDelete;
        ImageView productImage;
        RelativeLayout represent;
        ImageButton imgDecrease;
        ImageButton imgIncrease;
        EditText etCount;
        TextView txtCount;

        ViewHolder(View itemView) {
            super(itemView);

            productNameView = (TextView) itemView.findViewById(R.id.txtName);
            imgDelete = (ImageButton) itemView.findViewById(R.id.imgDelete);
            productImage = (ImageView) itemView.findViewById(R.id.imgProduct);
            represent = (RelativeLayout) itemView.findViewById(R.id.productRepresent);
            etCount = (EditText) itemView.findViewById(R.id.etCount);
            txtCount = (TextView) itemView.findViewById(R.id.txtCount);
            imgIncrease = (ImageButton) itemView.findViewById(R.id.imgIncrease);
            imgDecrease = (ImageButton) itemView.findViewById(R.id.imgDecrease);
        }
    }

    void remove(ListItem item) {
        // Не используем mObjects.remove(item), т.к. необходимо получить индекс удаленного элемента
        int i; boolean removed = false;
        for (i = 0; i < mObjects.size(); i++){
            if (mObjects.get(i).equals(item)) {
                mObjects.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, getItemCount());
        }
    }

    public void add(ListItem item) {
        mObjects.add(item);
        notifyItemRangeInserted(mObjects.size() - 1, mObjects.size());
    }

    public ListItem getItem(int position) {
        return (ListItem) mObjects.get(position);
    }

    public void updateItem(long id, String name, Uri imageUri) {
        for (ListItem item: (ArrayList<ListItem>)mObjects) {
            if (item.getId() == id){
                item.setName(name);
                item.setImageUri(imageUri);
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    private void setProductImage(ImageView imgProduct, Uri imageUri){
        if (imgProduct != null) {
            // Установим картинку
            Picasso.with(mParentActivity)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_default_product_image)
                    .fit()
                    .into(imgProduct);
        }
    }
}

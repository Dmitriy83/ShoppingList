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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

abstract public class BaseListAdapter extends RecyclerView.Adapter {

    final int mResource;
    final ArrayList<IListItem> mObjects;
    final Activity mParentActivity;

    BaseListAdapter(Context context, int resource, ArrayList<IListItem> objects) {
        super();

        mResource = resource;
        mObjects = objects;
        mParentActivity = (Activity) context;
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
        ViewHolder viewHolder = (ViewHolder) holder;
        IListItem item = mObjects.get(position);
        if (viewHolder.nameView != null) {
            viewHolder.nameView.setTag(item);
            // Заполним текстовое поле
            viewHolder.nameView.setText(item.getName());
        }
        if (viewHolder.represent != null) {
            viewHolder.represent.setTag(item);
            viewHolder.represent.setTag(R.string.view_holder, viewHolder);
        }
        if (viewHolder.imgDelete != null) {
            viewHolder.imgDelete.setTag(item);
        }
        if (viewHolder.etCount != null) {
            viewHolder.etCount.setTag(item);
            viewHolder.etCount.setText(String.format(Locale.ENGLISH, "%.1f", item.getCount()));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final ImageButton imgDelete;
        final ImageView itemImage;
        final RelativeLayout represent;
        final ImageButton imgDecrease;
        final ImageButton imgIncrease;
        final EditText etCount;
        final TextView txtCount;
        final LinearLayout llShoppingListInfo;
        final EditText etPrice;
        final TextView tvUnit;
        final RelativeLayout rlCount;
        final RelativeLayout rlCountAndUnit;
        final RelativeLayout rlCountAndPrice;

        ViewHolder(View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.txtName);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            itemImage = itemView.findViewById(R.id.imgItemImage);
            represent = itemView.findViewById(R.id.itemRepresent);
            txtCount = itemView.findViewById(R.id.txtCount);
            llShoppingListInfo = itemView.findViewById(R.id.llShoppingListInfo);
            etPrice = itemView.findViewById(R.id.etLastPrice);
            rlCount = itemView.findViewById(R.id.rlCount);
            rlCountAndUnit = itemView.findViewById(R.id.rlCountAndUnit);
            rlCountAndPrice = itemView.findViewById(R.id.rlCountAndPriceAndUnit);

            if (Utils.showPrices(mParentActivity)) {
                tvUnit = itemView.findViewById(R.id.tvUnit_CountAndPrice);
                etCount = itemView.findViewById(R.id.etCount_CountAndPrice);
                imgIncrease = itemView.findViewById(R.id.imgIncrease_CountAndPrice);
                imgDecrease = itemView.findViewById(R.id.imgDecrease_CountAndPrice);
            }else {
                tvUnit = itemView.findViewById(R.id.tvUnit_CountAndUnit);
                if (Utils.showUnits(mParentActivity)){
                    etCount = itemView.findViewById(R.id.etCount_CountAndUnit);
                    imgIncrease = itemView.findViewById(R.id.imgIncrease_CountAndUnit);
                    imgDecrease = itemView.findViewById(R.id.imgDecrease_CountAndUnit);
                }else {
                    etCount = itemView.findViewById(R.id.etCount);
                    imgIncrease = itemView.findViewById(R.id.imgIncrease);
                    imgDecrease = itemView.findViewById(R.id.imgDecrease);
                }
            }
        }
    }

    void remove(IListItem item) {
        // Не используем mObjects.remove(item), т.к. необходимо получить индекс удаленного элемента
        int i;
        boolean removed = false;
        for (i = 0; i < mObjects.size(); i++) {
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

    public void add(IListItem item) {
        mObjects.add(item);
        notifyItemRangeInserted(mObjects.size() - 1, mObjects.size());
    }

    public IListItem getItem(int position) {
        return mObjects.get(position);
    }

    public void updateItem(IListItem listItem) {
        for (IListItem item : mObjects)
            if (item.getId() == listItem.getId() && item.getClass() == listItem.getClass()) {
                item.setName(listItem.getName());
                item.setImageUri(listItem.getImageUri());
                if (listItem instanceof Product) {
                    Product product = (Product) item;
                    product.setCategory(((Product) listItem).getCategory());
                    product.setDefaultUnit(((Product) listItem).getDefaultUnit());
                    product.setLastPrice(((Product) listItem).getLastPrice());
                    product.setCurrentUnit(((Product) listItem).getCurrentUnit());
                    product.setCurrentPrice(((Product) listItem).getCurrentPrice());
                }
                if (listItem instanceof ShoppingList) {
                    ShoppingList sl = (ShoppingList) item;
                    sl.setNumberOfCrossedOutProducts(((ShoppingList)listItem).getNumberOfCrossedOutProducts());
                    sl.setTotalCountOfProducts(((ShoppingList)listItem).getTotalCountOfProducts());
                }
            }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    void setProductImage(final ImageView imgItemImage, IListItem item) {
        if (imgItemImage == null) return;

        final Uri imageUri = item.getImageUri();
        int placeholder = R.drawable.ic_default_product_image;
        if (item instanceof Product) {
            Product product = (Product) item;
            Category category = product.getCategory();
            if (category != null && category.getImageUri() != null && category.getImageUri() != Uri.EMPTY)
                placeholder = mParentActivity.getResources().getIdentifier(
                        category.getImageUri().toString(), null, mParentActivity.getPackageName());
        } else if (item instanceof Category) {
            Category category = (Category) item;
            if (category.getImageUri() != null && category.getImageUri() != Uri.EMPTY)
                placeholder = mParentActivity.getResources().getIdentifier(
                        imageUri.toString(), null, mParentActivity.getPackageName());
        }

        // Установим картинку
        final int finalPlaceholder = placeholder; // для использования в Callback
        Picasso.with(mParentActivity)
                .load(imageUri)
                .placeholder(placeholder)
                .fit()
                .into(imgItemImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Для поиска элемента при тестировании запишем imageId в contentDescription
                        imgItemImage.setContentDescription(String.valueOf(imageUri));
                    }

                    @Override
                    public void onError() {
                        // Для поиска элемента при тестировании запишем imageId в contentDescription
                        imgItemImage.setContentDescription(String.valueOf(finalPlaceholder));
                    }
                });
        // Если imageUri == null, то загрузится placeholder, но в метод onSuccess программа не зайдет
        if (imageUri == null || item instanceof Category)
            imgItemImage.setContentDescription(String.valueOf(finalPlaceholder));
    }
}

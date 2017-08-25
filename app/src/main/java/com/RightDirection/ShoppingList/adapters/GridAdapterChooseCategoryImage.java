package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ChooseCategoryImageActivity;
import com.squareup.picasso.Picasso;

public class GridAdapterChooseCategoryImage extends RecyclerView.Adapter {

    private final int[] mImageIds;

    // Store the context for easy access
    private final Context mContext;

    // Pass in the contact array into the constructor
    public GridAdapterChooseCategoryImage(Context context) {
        mContext = context;
        mImageIds = getImageIds();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    private static class ViewHolder extends RecyclerView.ViewHolder {
        // Our holder should contain a member variable
        // for any view that will be set as we render a row
        final ImageView imageView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View rowView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(rowView);

            imageView = (ImageView) rowView.findViewById(R.id.imageView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View rowView = inflater.inflate(R.layout.grid_item_category_image, parent, false);

        // Return a new holder instance
        return new ViewHolder(rowView);
    }

    private int[] getImageIds() {
        int[] array = new int[20];

        array[0] = R.drawable.category_alcohol;
        array[1] = R.drawable.category_bread;
        array[2] = R.drawable.category_candy;
        array[3] = R.drawable.category_drink;
        array[4] = R.drawable.category_fish;
        array[5] = R.drawable.category_fruits;
        array[6] = R.drawable.category_meat;
        array[7] = R.drawable.category_milk;
        array[8] = R.drawable.category_noodles;
        array[9] = R.drawable.category_vegetables;
        array[10] = R.drawable.category_add1;
        array[11] = R.drawable.category_add2;
        array[12] = R.drawable.category_add3;
        array[13] = R.drawable.category_add4;
        array[14] = R.drawable.category_add5;
        array[15] = R.drawable.category_household_chemicals;
        array[16] = R.drawable.category_pills;
        array[17] = R.drawable.category_hygienic_means;
        array[18] = R.drawable.category_electronics;
        array[19] = R.drawable.category_car;

        return array;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        final int imageId = mImageIds[position];

        // Set item views based on our views and data model
        final ViewHolder viewHolder = (ViewHolder) holder;

        // Установим картинку
        Picasso.with(mContext)
                .load(imageId)
                .placeholder(R.drawable.ic_default_product_image)
                .fit()
                .into(viewHolder.imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                // Для поиска элемента при тестировании запишем imageId в contentDescription
                                viewHolder.imageView.setContentDescription(String.valueOf(imageId));
                            }

                            @Override
                            public void onError() {
                                // Для поиска элемента при тестировании запишем imageId в contentDescription
                                viewHolder.imageView.setContentDescription(String.valueOf(R.drawable.ic_default_product_image));
                            }
                        }
                );

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCategoryImageActivity activity = (ChooseCategoryImageActivity) mContext;
                activity.sendImageUriAndFinish(mContext.getResources().getResourceName(mImageIds[viewHolder.getAdapterPosition()]));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageIds.length;
    }
}

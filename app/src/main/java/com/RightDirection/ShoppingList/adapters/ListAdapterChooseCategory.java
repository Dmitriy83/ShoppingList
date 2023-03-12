package com.RightDirection.ShoppingList.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;

import java.util.ArrayList;

public class ListAdapterChooseCategory extends RecyclerView.Adapter{
    private final ArrayList<Category> mCategories;

    // Store the context for easy access
    private final Context mContext;

    // Pass in the contact array into the constructor
    public ListAdapterChooseCategory(Context context, ArrayList<Category> categories) {
        mCategories = categories;
        mContext = context;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    private static class ViewHolder extends RecyclerView.ViewHolder {
        // Our holder should contain a member variable
        // for any view that will be set as we render a row
        public final TextView txtName;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View rowView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(rowView);

            txtName = rowView.findViewById(R.id.txtName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View rowView = inflater.inflate(R.layout.list_item_choose_category, parent, false);

        // Return a new holder instance
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        Category category = mCategories.get(position);

        // Set item views based on our views and data model
        final ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.txtName.setText(category.getName());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity)mContext;
                Intent intent = new Intent();
                intent.putExtra(EXTRAS_KEYS.CATEGORY.getValue(),
                        mCategories.get(viewHolder.getAdapterPosition()));
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }
}

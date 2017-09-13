package com.RightDirection.ShoppingList.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.Unit;

import java.util.ArrayList;

public class ListAdapterChooseUnit extends RecyclerView.Adapter{
    private final ArrayList<Unit> mUnits;

    // Store the context for easy access
    private final Context mContext;

    private Product mProduct;

    // Pass in the contact array into the constructor
    public ListAdapterChooseUnit(Context context, ArrayList<Unit> units) {
        mUnits = units;
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

            txtName = (TextView) rowView.findViewById(R.id.txtName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View rowView = inflater.inflate(R.layout.list_item_choose_unit, parent, false);

        // Return a new holder instance
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        Unit unit = mUnits.get(position);

        // Set item views based on our views and data model
        final ViewHolder viewHolder = (ViewHolder)holder;
        String name = "";
        if (unit.getName() != null){
            name = unit.getName();
        }
        String shortName = "";
        if (unit.getShortName() != null){
            shortName = unit.getShortName();
        }
        viewHolder.txtName.setText(name + ", " + shortName);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onItemViewClick(viewHolder); }
        });
    }

    private void onItemViewClick(ViewHolder viewHolder) {
        // Закрываем связанную с адаптером активность и передаем владельцу результат выбора
        Activity activity = (Activity)mContext;
        Intent intent = new Intent();
        Unit chosenUnit = mUnits.get(viewHolder.getAdapterPosition());
        intent.putExtra(EXTRAS_KEYS.UNIT.getValue(), chosenUnit); // Для случая вызова из активности ProductActivity
        mProduct.setCurrentUnit(chosenUnit); // Для случая вызова из активности ShoppingListEditingActivity
        intent.putExtra(EXTRAS_KEYS.PRODUCT.getValue(), mProduct); // Товар, отображение которого необходимо обновить
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @Override
    public int getItemCount() {
        return mUnits.size();
    }

    public void setProduct(Product product) {
        this.mProduct = product;
    }
}

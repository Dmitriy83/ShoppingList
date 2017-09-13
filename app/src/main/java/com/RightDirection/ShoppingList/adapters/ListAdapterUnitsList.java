package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Unit;

import java.util.ArrayList;

public class ListAdapterUnitsList extends BaseListAdapter {

    public ListAdapterUnitsList(Context context, ArrayList<IListItem> objects) {
        super(context, com.RightDirection.ShoppingList.R.layout.list_item_units_list, objects);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        if (viewHolder.represent != null)
            viewHolder.represent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onRepresentClick(view); }
            });
        if (viewHolder.imgDelete != null)
            viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onImageDeleteClick(view); }
            });
    }

    private void onImageDeleteClick(View view) {
        Unit unit = (Unit) view.getTag();
        unit.removeFromDB(mParentActivity);
        remove(unit);
    }

    private void onRepresentClick(View view) {
        Unit unit = (Unit) view.getTag();
        unit.startUnitActivity(mParentActivity);
    }
}

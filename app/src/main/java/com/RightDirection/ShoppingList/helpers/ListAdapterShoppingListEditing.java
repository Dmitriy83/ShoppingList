package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.views.SoftKeyboardListenedRelativeLayout;

import java.util.List;

public class ListAdapterShoppingListEditing extends ListAdapter {

    public ListAdapterShoppingListEditing(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Parameters parameters = new Parameters(position, convertView);
        parameters.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        // Привяжем к View объект ListItem
        parameters.viewHolder.imgDelete.setTag(parameters.item);

        if (parameters.viewHolder.parentRelativeLayout == null){
            parameters.viewHolder.parentRelativeLayout = (SoftKeyboardListenedRelativeLayout)mParentActivity.findViewById(R.id.shoppingListEditingContainerLayout);
        }
        parameters.viewHolder.parentRelativeLayout.setButtonsPanelPadding();

        return parameters.rowView;
    }

    private View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            ListItem item = (ListItem) view.getTag();
            // Удалим элемент списка
            remove(item);
            notifyDataSetChanged();
        }
    };
}

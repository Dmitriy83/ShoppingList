package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.views.SoftKeyboardListenedRelativeLayout;

import java.util.List;

public class ListAdapterShoppingListEditing extends ListAdapter {

    private SoftKeyboardListenedRelativeLayout mParentRelativeLayout;

    public ListAdapterShoppingListEditing(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TODO: Разобраться, почему getView вызывается так много раз

        Parameters parameters = new Parameters(position, convertView);
        parameters.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        // Привяжем к View объект ListItem
        parameters.viewHolder.imgDelete.setTag(parameters.item);

        // Для отработки смещения кнопки сохранения списка при заполнении списка
        if (mParentRelativeLayout == null) {
            mParentRelativeLayout = (SoftKeyboardListenedRelativeLayout) mParentActivity.findViewById(R.id.shoppingListEditingContainerLayout);
        }
        if (position == getCount() - 1){ // Минимизируем вызов процедуры
            Log.i("getView", "getView called, position: " + position + ".");
            mParentRelativeLayout.setButtonsPanelPadding();
        }

        return parameters.rowView;
    }

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            ListItem item = (ListItem) view.getTag();
            // Удалим элемент списка
            remove(item); // Оповещение об изменении не нужно, т.к. оно вызывается в самом методе remove

            Log.i("onImgDeleteClick", "onImgDeleteClick called, notifyDataSetChanged do not called.");
        }
    };
}

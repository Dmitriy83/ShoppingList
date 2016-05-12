package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;

import java.util.List;

public class ListAdapterShoppingListInShop extends ListAdapter {

    public ListAdapterShoppingListInShop(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Parameters parameters = new Parameters(position, convertView);

        parameters.viewHolder.productNameView.setOnTouchListener(onListItemTouch);

        // Отрисуем выбор товара
        if (parameters.item.isChecked()){
            setChecked(parameters.viewHolder.productNameView);
        }
        else{
            setUnchecked(parameters.viewHolder.productNameView);
        }

        return parameters.rowView;
    }

    float mInitXTouch = 0;
    float mEndXTouch = 0;

    private View.OnTouchListener onListItemTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                mInitXTouch = event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                // Получим объект item по элементу View
                ListItem item = (ListItem) v.getTag();

                mEndXTouch = event.getX();
                float distance = mEndXTouch - mInitXTouch;
                if (distance > 50){
                    setChecked((TextView)v);
                    item.setChecked();
                }
                else if(distance < -50){
                    setUnchecked((TextView)v);
                    item.setUnchecked();
                }
            }
            return true;
        }
    };

    private void setChecked(TextView v){
        // Покажем, что товар купили ("вычеркнем")
        v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setUnchecked(TextView v){
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        v.setPaintFlags(v.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
    }
}

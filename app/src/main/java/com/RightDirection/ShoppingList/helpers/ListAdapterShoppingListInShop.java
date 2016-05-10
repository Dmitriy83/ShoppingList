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

        LinearLayout listView;

        ListItem item = getItem(position);

        String name = item.getName();

        if (convertView == null){
            listView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(mResource, listView, true);
        }
        else{
            listView = (LinearLayout)convertView;
        }

        TextView productNameView = (TextView)listView.findViewById(R.id.itemName);
        productNameView.setOnTouchListener(onListItemTouch);
        productNameView.setText(name);

        return listView;
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
                mEndXTouch = event.getX();
                float distance = mEndXTouch - mInitXTouch;
                TextView tv = (TextView)v;
                if (distance > 50){
                    // Покажем, что товар купили ("вычеркнем")
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else if(distance < -50){
                    // Покажем, что  товар еще не купили (до этого выделили ошибочно)
                    tv.setPaintFlags(tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
            return true;
        }


    };
}

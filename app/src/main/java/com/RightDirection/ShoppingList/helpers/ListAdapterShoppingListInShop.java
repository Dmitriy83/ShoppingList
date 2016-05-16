package com.RightDirection.ShoppingList.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v7.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;

import java.util.List;
import java.util.logging.Handler;

public class ListAdapterShoppingListInShop extends ListAdapter {

    List<ListItem> mListObjects;

    public ListAdapterShoppingListInShop(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);

        mListObjects = objects;
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

                    // Если "вычеркнуты" все товары, выведем сообщение пользователю
                    if (allProductsChecked()){
                        //Toast.makeText(mParentActivity, mParentActivity.getString(R.string.in_shop_ending_work_message), Toast.LENGTH_SHORT).show();
                        AlertDialog alertDialog = new AlertDialog.Builder(
                                new ContextThemeWrapper(mParentActivity, mParentActivity.getApplicationInfo().theme)).create();
                        alertDialog.setMessage(mParentActivity.getString(R.string.in_shop_ending_work_message));
                        alertDialog.show();
                    }
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
        v.setBackgroundColor(Color.LTGRAY);
    }

    private boolean allProductsChecked() {
        boolean allProductsChecked = true;
        for (ListItem item: mListObjects) {
            if (!item.isChecked()){
                allProductsChecked = false;
                break;
            }
        }
        return allProductsChecked;
    }

    private void setUnchecked(TextView v){
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        v.setPaintFlags(v.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        v.setBackgroundColor(Color.WHITE);
    }
}

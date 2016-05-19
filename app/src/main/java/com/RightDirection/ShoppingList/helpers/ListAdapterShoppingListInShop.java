package com.RightDirection.ShoppingList.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterShoppingListInShop extends ListAdapter {

    private ArrayList<ListItem> mObjects;
    private ArrayList<ListItem> mOriginalValues;
    private boolean isFiltered;

    public ListAdapterShoppingListInShop(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);

        mObjects = objects;
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

                // Отфильтруем лист, если необходимо
                if (isFiltered){hideMarked();}
            }
            return true;
        }
    };

    private boolean allProductsChecked() {
        boolean allProductsChecked = true;
        for (ListItem item: mObjects) {
            if (!item.isChecked()){
                allProductsChecked = false;
                break;
            }
        }
        return allProductsChecked;
    }

    private void setChecked(TextView v){
        // Покажем, что товар купили ("вычеркнем")
        v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        v.setBackgroundColor(Color.LTGRAY);
    }

    private void setUnchecked(TextView v){
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        v.setPaintFlags(v.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        v.setBackgroundColor(Color.WHITE);
    }

    public boolean isFiltered(){
        return isFiltered;
    }

    public void showMarked() {
        isFiltered = false;

        // Восстановим первоначальный список
        mObjects.clear();
        mObjects.addAll(mOriginalValues);

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    public void hideMarked() {
        isFiltered = true;

        // При первом обращении сохраним первоначальный список
        if (mOriginalValues == null){
            mOriginalValues = new ArrayList<>(mObjects);
        }

        // Сначала восстановим первоначальный список, чтобы не потерять значения
        mObjects.clear();
        mObjects.addAll(mOriginalValues);

        // Удалим элементы из списка
        for (int i = mObjects.size() - 1; i >= 0; i -= 1) {
            ListItem item = mObjects.get(i);
            if (item.isChecked()){
                mObjects.remove(item);
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }
}

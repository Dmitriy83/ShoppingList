package com.RightDirection.ShoppingList.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;

import java.util.ArrayList;

public class ListAdapterShoppingListInShop extends ListAdapter {

    private ArrayList<Product> mOriginalValues;
    private boolean mIsFiltered;
    private boolean mCrossOutProduct;

    public ListAdapterShoppingListInShop(Context context, int resource, ArrayList<Product> objects) {
        super(context, resource, objects);

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCrossOutProduct = sharedPref.getBoolean(mContext.getString(R.string.pref_key_cross_out_action), true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewInitializer viewInitializer = new ViewInitializer(position, convertView);

        if (viewInitializer.viewHolder != null && viewInitializer.viewHolder.productRepresent != null)
            viewInitializer.viewHolder.productRepresent.setOnTouchListener(onProductTouch);

        // Отрисуем выбор товара
        if (viewInitializer.viewHolder != null && viewInitializer.viewHolder.productNameView != null) {
            if (viewInitializer.item.isChecked()) {
                setViewChecked(viewInitializer.viewHolder);
            } else {
                setViewUnchecked(viewInitializer.viewHolder);
            }
        }

        return viewInitializer.rowView;
    }

    private float mInitXTouch = 0;

    private final View.OnTouchListener onProductTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mCrossOutProduct && event.getAction() == MotionEvent.ACTION_UP){
                // Получим объект item, свзяанный с элементом View
                Product item = (Product) v.getTag();
                ViewHolder viewHolder = (ViewHolder) v.getTag(R.string.view_holder);

                if (!item.isChecked()){
                    setViewAndItemChecked(item, viewHolder);
                }
                else if (item.isChecked()){
                    setViewUnchecked(viewHolder);
                    item.setUnchecked();
                }

                // Отфильтруем лист, если необходимо
                if (mIsFiltered) hideMarked();
            }
            else if (mCrossOutProduct && event.getAction() == MotionEvent.ACTION_DOWN){
                mInitXTouch = event.getX();
            }
            else if (mCrossOutProduct && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
                // Получим объект item, свзяанный с элементом View
                Product item = (Product) v.getTag();

                float mEndXTouch = event.getX();
                float distance = mEndXTouch - mInitXTouch;
                ViewHolder viewHolder = (ViewHolder) v.getTag(R.string.view_holder);
                if (distance > 50){
                    setViewAndItemChecked(item, viewHolder);
                }
                else if(distance < -50){
                    if (viewHolder != null) {
                        setViewUnchecked(viewHolder);
                        item.setUnchecked();
                    }
                }

                // Отфильтруем лист, если необходимо
                if (mIsFiltered) hideMarked();
            }
            return true;
        }
    };

    private void setViewAndItemChecked(Product item, ViewHolder viewHolder) {
        if (viewHolder != null) {
            setViewChecked(viewHolder);
            item.setChecked();
        }
        // Если "вычеркнуты" все товары, выведем сообщение пользователю
        if (allProductsChecked()){
            showVictoryDialog();
        }
    }

    private void showVictoryDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(
                new ContextThemeWrapper(mParentActivity, mParentActivity.getApplicationInfo().theme)).create();
        alertDialog.setMessage(mParentActivity.getString(R.string.in_shop_ending_work_message));
        alertDialog.show();
        // Установим выравнивание текста по середине
        TextView messageText = (TextView)alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        messageText.setTextSize(mParentActivity.getResources().getDimensionPixelSize(R.dimen.victory_text_size));
    }

    private boolean allProductsChecked() {
        boolean allProductsChecked = true;
        for (Product item: (ArrayList<Product>)mObjects) {
            if (!item.isChecked()){
                allProductsChecked = false;
                break;
            }
        }
        return allProductsChecked;
    }

    private void setViewChecked(ViewHolder vh){
        // Покажем, что товар купили ("вычеркнем")
        vh.productNameView.setPaintFlags(vh.productNameView.getPaintFlags()
                | Paint.STRIKE_THRU_TEXT_FLAG);
        vh.productNameView.setBackgroundColor(Color.LTGRAY);

        // Установим такой же цвет и для количества и для картинки
        vh.txtCount.setBackgroundColor(Color.LTGRAY);
        vh.productImage.setBackgroundColor(Color.LTGRAY);
    }

    private void setViewUnchecked(ViewHolder vh){
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        vh.productNameView.setPaintFlags(vh.productNameView.getPaintFlags()
                & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        vh.productNameView.setBackgroundColor(Color.WHITE);

        // Установим такой же цвет и для количества и для картинки
        vh.txtCount.setBackgroundColor(Color.WHITE);
        vh.productImage.setBackgroundColor(Color.WHITE);
    }

    public boolean isFiltered(){
        return mIsFiltered;
    }

    public void setIsFiltered(boolean value){
        mIsFiltered = value;
    }

    public void showMarked() {
        mIsFiltered = false;

        // Восстановим первоначальный список
        mObjects.clear();
        mObjects.addAll(mOriginalValues);

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    public void hideMarked() {
        mIsFiltered = true;

        // При первом обращении сохраним первоначальный список
        if (mOriginalValues == null){
            mOriginalValues = new ArrayList<>(mObjects);
        }

        // Сначала восстановим первоначальный список, чтобы не потерять значения
        mObjects.clear();
        mObjects.addAll(mOriginalValues);

        // Удалим элементы из списка
        for (int i = mObjects.size() - 1; i >= 0; i -= 1) {
            Product item = (Product)mObjects.get(i);
            if (item.isChecked()){
                mObjects.remove(item);
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    public ArrayList<Product> getOriginalValues() {
        return mOriginalValues;
    }

    public void setOriginalValues(ArrayList<Product> originalValues) {
        mOriginalValues = originalValues;
    }
}

package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.utils.DecimalDigitsInputFilter;
import com.RightDirection.ShoppingList.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ListAdapterShoppingListEditing extends BaseListAdapter {

    private Timer mTimer;

    public ListAdapterShoppingListEditing(Context context, int resource, ArrayList<IListItem> products) {
        super(context, resource, products);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        
        if (viewHolder.itemImage != null) {
            if (Utils.showImages(mParentActivity)) {
                viewHolder.itemImage.setVisibility(View.VISIBLE);
                setProductImage(viewHolder.itemImage, mObjects.get(position));
            } else {
                viewHolder.itemImage.setVisibility(View.GONE);
            }
        }
        if (viewHolder.imgDelete != null)
            viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
        if (viewHolder.represent != null)
            viewHolder.represent.setOnLongClickListener(onProductRepresentLongClick);
        if (viewHolder.imgDecrease != null)
            viewHolder.imgDecrease.setOnTouchListener(new PlusMinusOnTouchListener(-1));
        if (viewHolder.imgIncrease != null)
            viewHolder.imgIncrease.setOnTouchListener(new PlusMinusOnTouchListener(1));
        if (viewHolder.etCount != null) {
            viewHolder.etCount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(10, 1)}); // ограничим ввод одним знаком после запятой
            viewHolder.etCount.setOnFocusChangeListener(onEtCountFocusChange);
        }
    }

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Получим объект item по элементу View
            Product item = (Product) view.getTag();
            // Удалим элемент списка
            remove(item); // Оповещение об изменении не нужно, т.к. оно вызывается в самом методе remove
        }
    };

    private final View.OnFocusChangeListener onEtCountFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                // Получим объект item по элементу View
                Product item = (Product) view.getTag();
                // Изменим количество
                EditText etCount = (EditText) view;
                item.setCount(etCount.getText().toString());
            }
        }
    };

    private class PlusMinusOnTouchListener implements View.OnTouchListener{
        final int mIncrement;

        PlusMinusOnTouchListener(int increment){
            mIncrement = increment;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                startTimer(v, mIncrement, 1000, 100, false);
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP){
                stopTimer();
                changeCount(v, mIncrement);
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_CANCEL){
                stopTimer();
                return true;
            }
            return false;
        }
    }

    private void startTimer(View view, int increment, int delay, int period, boolean accelerated){
        mTimer = new Timer();
        IncrementTimerTask mTimerTask = new IncrementTimerTask(view, increment, accelerated);
        mTimer.schedule(mTimerTask, delay, period);
    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private class IncrementTimerTask extends TimerTask {
        private static final long ACCELERATE_PERIOD = 5000;
        final View mView;
        int mIncrement = 0;
        long mStartTime = 0;
        final boolean mAccelerated;

        IncrementTimerTask(View view, int increment, boolean accelerated){
            mView = view;
            mIncrement = increment;
            mStartTime = System.currentTimeMillis();
            mAccelerated = accelerated;
        }

        @Override
        public void run() {
            if (!mAccelerated) {
                long period = System.currentTimeMillis() - mStartTime;
                if (period > ACCELERATE_PERIOD) {
                    // Ускорим приращение, перезапустив таймер
                    stopTimer();
                    startTimer(mView, mIncrement, 0, 20, true);
                }
            }

            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeCount(mView, mIncrement);
                }
            });
        }
    }

    private void changeCount(View view, int increment){
        // Получим объект item по элементу View
        Product item = (Product) view.getTag(R.id.item);

        // Если текстовое поле находится в фокусе, то сначала нужно получить значение из него
        EditText etCount = (EditText) view.getTag(R.id.etCount);
        String etCountText = etCount.getText().toString();
        if (!etCountText.equals(String.valueOf(item.getCount())))
            item.setCount(etCount.getText().toString());

        // Изменим количество
        float count = new BigDecimal(item.getCount() + increment)
                .setScale(1, RoundingMode.HALF_UP).floatValue();
        item.setCount(count);
        etCount.setText(String.valueOf(item.getCount()));
    }


    private final View.OnLongClickListener onProductRepresentLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Product product = (Product) view.getTag();
            // Откроем активность редактирования продукта
            product.startProductActivity(mParentActivity);
            return false;
        }
    };
}

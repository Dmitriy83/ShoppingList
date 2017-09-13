package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ChooseUnitActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.utils.DecimalDigitsInputFilter;
import com.RightDirection.ShoppingList.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ListAdapterShoppingListEditing extends BaseListAdapter {

    private Timer mTimer;
    private static final String TAG = ListAdapterShoppingListEditing.class.getSimpleName();

    public ListAdapterShoppingListEditing(Context context, int resource, ArrayList<IListItem> products) {
        super(context, resource, products);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        IListItem item = mObjects.get(position);
        if (item != null && item instanceof Product) {
            Product product = (Product)item;
            if (viewHolder.imgDecrease != null) {
                viewHolder.imgDecrease.setTag(R.id.item, item);
                viewHolder.imgDecrease.setTag(R.id.etCount, viewHolder.etCount);
            }
            if (viewHolder.imgIncrease != null) {
                viewHolder.imgIncrease.setTag(R.id.item, item);
                viewHolder.imgIncrease.setTag(R.id.etCount, viewHolder.etCount);
            }

            if (viewHolder.itemImage != null) {
                if (Utils.showImages(mParentActivity)) {
                    viewHolder.itemImage.setVisibility(View.VISIBLE);
                    setProductImage(viewHolder.itemImage, item);
                } else {
                    viewHolder.itemImage.setVisibility(View.GONE);
                }
            }
            if (viewHolder.imgDelete != null)
                viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onImgDeleteClick(view);
                    }
                });
            if (viewHolder.represent != null)
                viewHolder.represent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return onProductRepresentLongClick(view);
                    }
                });
            if (viewHolder.imgDecrease != null)
                viewHolder.imgDecrease.setOnTouchListener(new PlusMinusOnTouchListener(-1));
            if (viewHolder.imgIncrease != null)
                viewHolder.imgIncrease.setOnTouchListener(new PlusMinusOnTouchListener(1));
            if (viewHolder.etCount != null) {
                viewHolder.etCount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(1)}); // ограничим ввод одним знаком после запятой
                viewHolder.etCount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        onEtCountFocusChange(view, hasFocus);
                    }
                });
            }

            if (Utils.showPrices(mParentActivity)) {
                viewHolder.rlCount.setVisibility(View.GONE);
                viewHolder.rlCountAndUnit.setVisibility(View.GONE);
                viewHolder.rlCountAndPrice.setVisibility(View.VISIBLE);

                viewHolder.etPrice.setTag(item);
                viewHolder.etPrice.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)}); // ограничим ввод двумя знаками после запятой
                viewHolder.etPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        onEtPriceFocusChange(view, hasFocus);
                    }
                });

                viewHolder.etPrice.setText(String.format(Locale.ENGLISH, "%.2f", product.getPrice()));
            } else if (Utils.showUnits(mParentActivity)) {
                viewHolder.rlCount.setVisibility(View.GONE);
                viewHolder.rlCountAndUnit.setVisibility(View.VISIBLE);
                viewHolder.rlCountAndPrice.setVisibility(View.GONE);
            } else {
                viewHolder.rlCount.setVisibility(View.VISIBLE);
                viewHolder.rlCountAndUnit.setVisibility(View.GONE);
                viewHolder.rlCountAndPrice.setVisibility(View.GONE);
            }

            if (Utils.showUnits(mParentActivity)) {
                viewHolder.tvUnit.setTag(item);
                viewHolder.tvUnit.setText(product.getUnitShortName(mParentActivity));
                viewHolder.tvUnit.setVisibility(View.VISIBLE);
                viewHolder.tvUnit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onTvUnitClick(v);
                    }
                });
            } else {
                viewHolder.tvUnit.setVisibility(View.GONE);
            }
        }
    }

    private void onTvUnitClick(View v) {
        Product product = (Product) v.getTag();
        Intent intent = new Intent(mParentActivity, ChooseUnitActivity.class);
        intent.putExtra(EXTRAS_KEYS.PRODUCT.getValue(), product);
        mParentActivity.startActivityForResult(intent, Utils.GET_UNIT);
    }

    private void onImgDeleteClick(View view) {
        // Получим объект item по элементу View
        Product item = (Product) view.getTag();
        // Удалим элемент списка
        remove(item); // Оповещение об изменении не нужно, т.к. оно вызывается в самом методе remove
        refreshTotalSum();
    }

    private void onEtCountFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            // Получим объект item по элементу View
            Product item = (Product) view.getTag();
            // Изменим количество
            EditText etCount = (EditText) view;
            item.setCount(etCount.getText().toString());
            refreshTotalSum();
        }
    }

    private void onEtPriceFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            // Получим объект по элементу View
            Product product = (Product) view.getTag();
            // Изменим текущую цену
            EditText etPrice = (EditText) view;
            try{
                float currentPrice = Float.valueOf(etPrice.getText().toString());
                // Текущую цену меняем в случаях, если:
                //  1) Измененная цена отличается от установленной ранее текущей цены;
                //  2) Текущая цена до этого не изменялась и новая цена не равна цене по умолчанию
                if (currentPrice != product.getCurrentPrice()
                        && (!(currentPrice == Product.EMPTY_CURRENT_PRICE && currentPrice == product.getLastPrice()))
                        ){
                    product.setCurrentPrice(currentPrice);
                    refreshTotalSum();
                }
            }catch(Exception e){
                // Не удалось преобразовать в число
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void refreshTotalSum() {
        if (mParentActivity instanceof ShoppingListEditingActivity) {
            Utils.calculateTotalSum(mParentActivity, mObjects);
        }
    }

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
                refreshTotalSum();
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

    private boolean onProductRepresentLongClick(View view) {
        Product product = (Product) view.getTag();
        // Откроем активность редактирования продукта
        product.startProductActivity(mParentActivity);
        return false;
    }
}

package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.RightDirection.ShoppingList.Product;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ItemActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ListAdapterShoppingListEditing extends ListAdapter {

    Timer mTimer;
    IncrementTimerTask mTimerTask;

    public ListAdapterShoppingListEditing(Context context, int resource, ArrayList<Product> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewInitializer viewInitializer = new ViewInitializer(position, convertView);
        if (viewInitializer.viewHolder != null){
            if (viewInitializer.viewHolder.imgDecrease == null) viewInitializer.viewHolder.imgDecrease
                    = (ImageButton) viewInitializer.rowView.findViewById(R.id.imgDecrease);
            if (viewInitializer.viewHolder.imgIncrease == null) viewInitializer.viewHolder.imgIncrease
                    = (ImageButton) viewInitializer.rowView.findViewById(R.id.imgIncrease);

            if (viewInitializer.viewHolder.imgDelete != null)
                viewInitializer.viewHolder.imgDelete.setOnClickListener(onImgDeleteClick);
            if (viewInitializer.viewHolder.productRepresent != null)
                viewInitializer.viewHolder.productRepresent.setOnLongClickListener(onProductRepresentLongClick);
            if (viewInitializer.viewHolder.imgDecrease != null) {
                viewInitializer.viewHolder.imgDecrease.setTag(R.id.item, viewInitializer.item);
                viewInitializer.viewHolder.imgDecrease.setTag(R.id.etCount, viewInitializer.viewHolder.etCount);
                viewInitializer.viewHolder.imgDecrease.setOnTouchListener(onImgDecreaseTouch);
            }
            if (viewInitializer.viewHolder.imgIncrease != null) {
                viewInitializer.viewHolder.imgIncrease.setTag(R.id.item, viewInitializer.item);
                viewInitializer.viewHolder.imgIncrease.setTag(R.id.etCount, viewInitializer.viewHolder.etCount);
                viewInitializer.viewHolder.imgIncrease.setOnTouchListener(onImgIncreaseTouch);
            }
            if (viewInitializer.viewHolder.etCount != null) {
                viewInitializer.viewHolder.etCount.setOnFocusChangeListener(onEtCountFocusChange);
            }
        }

        return viewInitializer.rowView;
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

    private final View.OnTouchListener onImgIncreaseTouch = new View.OnTouchListener() {
        private final static int INCREMENT = 1;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (!(event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL)){
                return false;
            }

            onTouchEvent(view, event, INCREMENT);
            return true;
        }
    };

    private final View.OnTouchListener onImgDecreaseTouch = new View.OnTouchListener() {
        private final static int INCREMENT = -1;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (!(event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL)){
                return false;
            }

            onTouchEvent(view, event, INCREMENT);
            return true;
        }
    };

    void onTouchEvent(View view, MotionEvent event, int increment){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            changeCount(view, increment);
            startTimer(view, increment, 1000, 100, false);
        }else{
            stopTimer();
        }
    }

    void startTimer(View view, int increment, int delay, int period, boolean accelerated){
        mTimer = new Timer();
        mTimerTask = new IncrementTimerTask(view, increment, accelerated);
        mTimer.schedule(mTimerTask, delay, period);
    }

    void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    class IncrementTimerTask extends TimerTask {
        private static final long ACCELERATE_PERIOD = 5000;
        View mView;
        int mIncrement = 0;
        long mStartTime = 0;
        boolean mAccelerated;

        public IncrementTimerTask(View view, int increment, boolean accelerated){
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

    public void changeCount(View view, int increment){
        // Получим объект item по элементу View
        Product item = (Product) view.getTag(R.id.item);

        // Если текстовое поле находится в фокусе, то сначала нужно получить значение из него
        EditText etCount = (EditText) view.getTag(R.id.etCount);
        String etCountText = etCount.getText().toString();
        if (!etCountText.equals(String.valueOf(item.getCount())))
            item.setCount(etCount.getText().toString());

        // Изменим количество
        item.setCount(item.getCount() + increment);
        etCount.setText(String.valueOf(item.getCount()));
    }


    private final View.OnLongClickListener onProductRepresentLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Product item = (Product) view.getTag();

            Intent intent = new Intent(mParentActivity.getBaseContext(), ItemActivity.class);
            intent.putExtra(String.valueOf(R.string.name), item.getName());
            intent.putExtra(String.valueOf(R.string.item_id), item.getId());
            intent.putExtra(String.valueOf(R.string.item_image), item.getImageUri());
            intent.putExtra(String.valueOf(R.string.is_new_item), false);
            mParentActivity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);

            return false;
        }
    };
}

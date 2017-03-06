package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.DoneActivity;
import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.Product;

import java.util.ArrayList;

public class ListAdapterShoppingListInShop extends ListAdapter {

    private ArrayList<IListItem> mOriginalValues;
    private boolean mIsFiltered;
    private final boolean mCrossOutProduct;

    public ListAdapterShoppingListInShop(Context context, int resource, ArrayList<IListItem> objects) {
        super(context, resource, objects);

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mParentActivity);
        mCrossOutProduct = sharedPref.getBoolean(mParentActivity.getString(R.string.pref_key_cross_out_action), true);
    }

    @Override
    public int getItemViewType(int position) {
        IListItem item = mObjects.get(position);
        return item.getType().getValue();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView;
        if (viewType == ITEM_TYPES.CATEGORY.getValue()) {
            // Создаем элемент-заголовок
            rowView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_category_title, parent, false);
        } else {
            // Создаем обычный элемент списка
            rowView = LayoutInflater.from(parent.getContext())
                    .inflate(mResource, parent, false);
        }

        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        if (viewHolder != null && viewHolder.represent != null) {
            viewHolder.represent.setOnTouchListener(onProductTouch);
            viewHolder.represent.setOnLongClickListener(onRepresentLongClick);
        }

        // Отрисуем выбор товара
        IListItem item = mObjects.get(position);
        if (item instanceof Product) {
            if (item.isChecked()) {
                setViewChecked(viewHolder);
            } else {
                setViewUnchecked(viewHolder);
            }
        }
    }

    private float mInitXTouch = 0;
    // Если произошло движение пальца, то обрабатывать долгое нажатие не нужно
    private boolean mFingerMoved = false;

    private final View.OnTouchListener onProductTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mCrossOutProduct && event.getAction() == MotionEvent.ACTION_UP) {
                // Получим объект item, свзяанный с элементом View
                Product item = (Product) v.getTag();
                ViewHolder viewHolder = (ViewHolder) v.getTag(R.string.view_holder);

                if (!item.isChecked()) {
                    setViewAndItemChecked(item, viewHolder);
                } else if (item.isChecked()) {
                    setViewUnchecked(viewHolder);
                    item.setUnchecked();
                }

                // Отфильтруем лист, если необходимо
                if (mIsFiltered) hideChecked();
            } else if (mCrossOutProduct && event.getAction() == MotionEvent.ACTION_DOWN) {
                mInitXTouch = event.getX();
            } else if (mCrossOutProduct && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                // Получим объект item, свзяанный с элементом View
                Product item = (Product) v.getTag();

                float mEndXTouch = event.getX();
                float distance = mEndXTouch - mInitXTouch;
                ViewHolder viewHolder = (ViewHolder) v.getTag(R.string.view_holder);
                if (distance > 50 || distance < -50) {
                    if (item.isChecked())
                        setViewAndItemUnchecked(item, viewHolder);
                    else
                        setViewAndItemChecked(item, viewHolder);
                }

                // Отфильтруем лист, если необходимо
                if (mIsFiltered) hideChecked();

                mFingerMoved = false;
            }else if (mCrossOutProduct && event.getAction() == MotionEvent.ACTION_MOVE){
                float distance = event.getX() - mInitXTouch;
                // Исключим мнезначительное "дергание" пальца
                if (distance > 1 || distance < -1) mFingerMoved = true;
            }

            return false;   // false означает, что другие обработчики события (например, onLongClick)
            // также следует использовать
        }
    };

    private final View.OnLongClickListener onRepresentLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (mFingerMoved) return false;
            Product product = (Product) view.getTag();
            // Откроем активность редактирования продукта
            product.startProductActivity(mParentActivity);
            return false;
        }
    };

    private void setViewAndItemChecked(Product item, ViewHolder viewHolder) {
        if (viewHolder != null) {
            setViewChecked(viewHolder);
            item.setChecked();
        }
        // Если "вычеркнуты" все товары, выведем сообщение пользователю
        if (allProductsChecked()) {
            showDoneMessage();
        }
    }

    private void setViewAndItemUnchecked(Product item, ViewHolder viewHolder) {
        if (viewHolder != null) {
            setViewUnchecked(viewHolder);
            item.setUnchecked();
        }
    }

    private void showDoneMessage() {
        Intent intent = new Intent(mParentActivity, DoneActivity.class);
        mParentActivity.startActivity(intent);
    }

    private boolean allProductsChecked() {
        boolean allProductsChecked = true;
        for (IListItem item : mObjects) {
            if (item instanceof Product && !item.isChecked()) {
                allProductsChecked = false;
                break;
            }
        }
        return allProductsChecked;
    }

    private void setViewChecked(ViewHolder vh) {
        // Покажем, что товар купили ("вычеркнем")
        vh.productNameView.setPaintFlags(vh.productNameView.getPaintFlags()
                | Paint.STRIKE_THRU_TEXT_FLAG);
        vh.txtCount.setPaintFlags(vh.txtCount.getPaintFlags()
                | Paint.STRIKE_THRU_TEXT_FLAG);
        vh.represent.setBackgroundColor(Color.LTGRAY);
    }

    private void setViewUnchecked(ViewHolder vh) {
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        vh.productNameView.setPaintFlags(vh.productNameView.getPaintFlags()
                & (~Paint.STRIKE_THRU_TEXT_FLAG));
        vh.txtCount.setPaintFlags(vh.txtCount.getPaintFlags()
                & (~Paint.STRIKE_THRU_TEXT_FLAG));
        vh.represent.setBackgroundColor(Color.WHITE);
    }

    public boolean isFiltered() {
        return mIsFiltered;
    }

    public void setIsFiltered(boolean value) {
        mIsFiltered = value;
    }

    public void deselectAll(){
        if (mOriginalValues == null){ // фильтрация еще не устанавливалась
            for (IListItem product: mObjects) {
                product.setUnchecked();
            }
        } else{
            for (IListItem product: mOriginalValues) {
                product.setUnchecked();
            }
            retrieveOriginalValues();
        }
    }

    public void  showChecked() {
        mIsFiltered = false;

        // Восстановим первоначальный список
        retrieveOriginalValues();

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    private void retrieveOriginalValues() {
        mObjects.clear();
        mObjects.addAll(mOriginalValues);
    }

    public void hideChecked() {
        mIsFiltered = true;

        // При первом обращении сохраним первоначальный список
        if (mOriginalValues == null) {
            mOriginalValues = new ArrayList<>(mObjects);
        }

        // Сначала восстановим первоначальный список, чтобы не потерять значения
        retrieveOriginalValues();

        // Удалим "вычеркнутые" продукты и категории из списка
        for (int i = mObjects.size() - 1; i >= 0; i--) {
            IListItem item = mObjects.get(i);
            if (item instanceof Product && item.isChecked()) {
                mObjects.remove(item);
            }

            // Удаляем категории с полностью вычеркнутыми товарами
            if (item instanceof Category) {
                if (i + 1 <= mObjects.size() - 1) {
                    // Если следующий за категорией элемент является категорией, значит все
                    // продукты данной категории вычеркнуты и категорию следует удалить
                    IListItem nextItem = mObjects.get(i + 1);
                    if (nextItem instanceof Category) {
                        mObjects.remove(item);
                    }
                } else {
                    // Категория является последним элементом в массиве. Значит все продукты
                    // данной категории вычеркнуты, и категорию следует удалить
                    mObjects.remove(item);
                }
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }

    public ArrayList<IListItem> getOriginalValues() {
        if (mOriginalValues == null) return mObjects;
        else return mOriginalValues;
    }

    public void setOriginalValues(ArrayList<IListItem> originalValues) {
        mOriginalValues = originalValues;
    }
}

package com.RightDirection.ShoppingList.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ProductActivity;
import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.items.Category;
import com.RightDirection.ShoppingList.items.ListItem;
import com.RightDirection.ShoppingList.items.Product;
import com.RightDirection.ShoppingList.utils.Utils;

import java.util.ArrayList;

public class ListAdapterShoppingListInShop extends ListAdapter {

    private ArrayList<Product> mOriginalValues;
    private boolean mIsFiltered;
    private boolean mCrossOutProduct;

    public ListAdapterShoppingListInShop(Context context, int resource, ArrayList<Product> objects) {
        super(context, resource, objects);

        // Прочитаем настройки приложения
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mParentActivity);
        mCrossOutProduct = sharedPref.getBoolean(mParentActivity.getString(R.string.pref_key_cross_out_action), true);
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = (ListItem)mObjects.get(position);
        return item.getType().getNumValue();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView;
        if (viewType == ITEM_TYPES.CATEGORY.getNumValue()) {
            // Создаем элемент-заголовок
            rowView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_category_title, parent, false);
        }else {
            // Создаем обычный элемент списка
            rowView = LayoutInflater.from(parent.getContext())
                    .inflate(mResource, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(rowView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder)holder;
        if (viewHolder != null && viewHolder.represent != null) {
            viewHolder.represent.setOnTouchListener(onProductTouch);
            viewHolder.represent.setOnLongClickListener(onRepresentLongClick);
        }

        // Отрисуем выбор товара
        if (viewHolder != null && viewHolder.productNameView != null) {
            ListItem item = (ListItem)mObjects.get(position);
            if (item.isChecked()) {
                setViewChecked(viewHolder);
            } else {
                setViewUnchecked(viewHolder);
            }
        }
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
            return false;   // false означает, что другие обработчики события (например, onLongClick)
                            // также следует использовать
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
        for (ListItem item: (ArrayList<ListItem>)mObjects) {
            if (item instanceof Product && !item.isChecked()){
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
        if (vh.txtCount != null) { // не заголовок
            vh.txtCount.setBackgroundColor(Color.LTGRAY);
            if (vh.productImage != null) vh.productImage.setBackgroundColor(Color.LTGRAY);
        }
    }

    private void setViewUnchecked(ViewHolder vh){
        // Покажем, что  товар еще не купили (до этого выделили ошибочно)
        vh.productNameView.setPaintFlags(vh.productNameView.getPaintFlags()
                & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        vh.productNameView.setBackgroundColor(Color.WHITE);

        // Установим такой же цвет и для количества и для картинки
        if (vh.txtCount != null) { // не заголовок
            vh.txtCount.setBackgroundColor(Color.WHITE);
            if (vh.productImage != null) vh.productImage.setBackgroundColor(Color.WHITE);
        }
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

        // Удалим "вычеркнутые" продукты и категории из списка
        for (int i = mObjects.size() - 1; i >= 0; i--) {
            ListItem item = (ListItem)mObjects.get(i);
            if (item instanceof Product && item.isChecked()){
                mObjects.remove(item);
            }

            // Удаляем категории с полностью вычеркнутыми товарами
            if (item instanceof Category){
                if (i + 1 <= mObjects.size() - 1) {
                    // Если следующий за категорией элемент является категорией, значит все
                    // продукты данной категории вычеркнуты и категорию следует удалить
                    ListItem nextItem = (ListItem) mObjects.get(i + 1);
                    if (nextItem instanceof Category){
                        mObjects.remove(item);
                    }
                }else{
                    // Категория является последним элементом в массиве. Значит все продукты
                    // данной категории вычеркнуты, и категорию следует удалить
                    mObjects.remove(item);
                }
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

    private final View.OnLongClickListener onRepresentLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Product product = (Product) view.getTag();

            // Откроем активность редактирования продукта
            Intent intent = new Intent(mParentActivity.getBaseContext(), ProductActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_item), false);
            intent.putExtra(String.valueOf(R.string.product), product);
            intent.putExtra(String.valueOf(R.string.category), product.getCategory());
            mParentActivity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);

            return false;
        }
    };

    public void updateItem(long id, String name, Uri imageUri, Category category) {
        for (ListItem item: (ArrayList<ListItem>)mObjects) {
            if (item.getId() == id){
                item.setName(name);
                item.setImageUri(imageUri);
                if (item instanceof Product) {
                    ((Product) item).setCategory(category);
                }
            }
        }

        // Оповестим об изменении данных
        this.notifyDataSetChanged();
    }
}
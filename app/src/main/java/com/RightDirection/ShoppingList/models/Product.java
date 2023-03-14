package com.RightDirection.ShoppingList.models;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.ProductActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

public class Product extends ListItem implements IDataBaseOperations {

    private Category category;
    private long rowId;
    private Unit defaultUnit;
    private Unit currentUnit;
    private float lastPrice;
    private float currentPrice = EMPTY_CURRENT_PRICE;
    public static final float EMPTY_CURRENT_PRICE = -999.99999f;

    public Product(@SuppressWarnings("SameParameterValue") long id) {
        super(id, "");
    }

    public Product(long id, String name) {
        super(id, name, null);
    }

    public Product(long id, String name, float count) {
        super(id, name, count);
    }

    public Product(@SuppressWarnings("SameParameterValue") long id, String name, float count, float currentPrice, Unit currentUnit) {
        super(id, name, count);
        this.currentPrice = currentPrice;
        this.currentUnit = currentUnit;
    }

    public Product(long id, String name, float count, boolean isChecked) {
        super(id, name, count);

        this.isChecked = isChecked;
    }

    public Product(Cursor data, Category category, Unit defaultUnit, Unit currentUnit){
        super(data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID)),
                data.getString(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_NAME)),
                SL_ContentProvider.getImageUri(data));

        try {
            this.count = data.getFloat(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_COUNT));
            this.isChecked = data.getInt(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_IS_CHECKED)) != 0;
            this.rowId =  data.getLong(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_SHOPPING_LIST_ROW_ID));
            this.currentPrice = data.getFloat(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRICE));
        } catch (Exception e){
            // Столбец не найден.Например, при вызове из InputProductNameFragment
            this.count = 1;
            this.isChecked = false;
            this.rowId = 0;
            this.currentPrice = EMPTY_CURRENT_PRICE;
        }

        this.category = category;
        this.defaultUnit = defaultUnit;
        this.currentUnit = currentUnit;
        this.lastPrice = data.getFloat(data.getColumnIndexOrThrow(SL_ContentProvider.KEY_LAST_PRICE));
    }

    private Product(Parcel in) {
        super(in);
        category = in.readParcelable(Category.class.getClassLoader());
        rowId = in.readLong();
        defaultUnit = in.readParcelable(Unit.class.getClassLoader());
        currentUnit = in.readParcelable(Unit.class.getClassLoader());
        lastPrice = in.readFloat();
        currentPrice = in.readFloat();
    }

    @SuppressWarnings("unused")
    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(category, flags);
        dest.writeLong(rowId);
        dest.writeParcelable(defaultUnit, flags);
        dest.writeParcelable(currentUnit, flags);
        dest.writeFloat(lastPrice);
        dest.writeFloat(currentPrice);
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Unit getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(Unit defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    public Unit getCurrentUnit() {
        return currentUnit;
    }

    public void setCurrentUnit(Unit currentUnit) {
        this.currentUnit = currentUnit;
    }

    /**
     * Получение сокращенного наименования единицы измерения.
     * Сначала производится попытка получения из текущей для списка ед. измерения, потом - из
     * ед. измерения по умолчанию для товара. Если не найдена, то возвращется сокращенное название
     * ед. измерения по умолчанию для приложения.
     *
     * @param context Контекст выполнения
     * @return Сокращенное наименование ед. измерения товара
     */
    public String getUnitShortName(Context context){
        Unit currentUnit = getCurrentUnit();
        if (currentUnit != null && currentUnit.getShortName() != null) {
            return currentUnit.getShortName();
        }else{
            // Попробуем получить ед. измерения по умолчанию
            Unit defaultUnit = getDefaultUnit();
            if (defaultUnit != null  && defaultUnit.getShortName() != null) {
                return defaultUnit.getShortName();
            }
        }

        // Возвращаем сокращение единицы измерения по умолчанию
        return context.getString(R.string.default_unit);
    }

    /**
     * Получение актуальной цены продукта.
     * Сначала производится попытка получения текущей для списка цены, потом - цены
     * по умолчанию для товара. Если не найдена, то возвращется 0.
     *
     * @return Атуальная цена товара
     */
    public float getPrice(){
        if (getCurrentPrice() != Product.EMPTY_CURRENT_PRICE) {
            return getCurrentPrice();
        } else {
            return getLastPrice();
        }
    }

    public float getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(float lastPrice) {
        this.lastPrice = lastPrice;
    }

    @Override
    public void addToDB(Context context) {
        // Продукт более не новый
        isNew = false;

        ContentResolver contentResolver = context.getContentResolver();

        // Если товар с данным именем уже есть в БД, то создавать новый не нужно.
        // Необходимо присвоить id найденного элемента текущему.
        Cursor data = contentResolver.query(SL_ContentProvider.PRODUCTS_CONTENT_URI, null,
                SL_ContentProvider.KEY_NAME + " = ?", new String[]{getName()}, null);
        if (data != null){
            if (data.moveToNext()) {
                int keyIdIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRODUCT_ID);
                setId(data.getLong(keyIdIndex));
                Toast.makeText(context, context.getString(R.string.product_is_exist),
                        Toast.LENGTH_SHORT).show();
                data.close();
                return;
            }
            data.close();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_NAME, getName());
        if (category != null)
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_ID, category.getId());
        if (getImageUri() != null)
            contentValues.put(SL_ContentProvider.KEY_PICTURE, getImageUri().toString());
        if (defaultUnit != null)
            contentValues.put(SL_ContentProvider.KEY_DEFAULT_UNIT_ID, defaultUnit.getId());
        contentValues.put(SL_ContentProvider.KEY_LAST_PRICE, getLastPrice());
        Uri insertedId = contentResolver.insert(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));

        addPriceChangeHistory(context);
    }

    @Override
    public void removeFromDB(Context context) {
        // Удалим запись из БД по id
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                SL_ContentProvider.KEY_ID + "= ?", new String[]{String.valueOf(getId())});
    }

    @Override
    public void updateInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_NAME, getName());
        if (category != null) {
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_ID, category.getId());
        }else{
            contentValues.put(SL_ContentProvider.KEY_CATEGORY_ID, 0);
        }
        if (getImageUri() != null) {
            contentValues.put(SL_ContentProvider.KEY_PICTURE, getImageUri().toString());
        }else{
            contentValues.put(SL_ContentProvider.KEY_PICTURE, (String) null);
        }
        if (defaultUnit != null)
            contentValues.put(SL_ContentProvider.KEY_DEFAULT_UNIT_ID, defaultUnit.getId());
        contentValues.put(SL_ContentProvider.KEY_LAST_PRICE, getLastPrice());
        contentResolver.update(SL_ContentProvider.PRODUCTS_CONTENT_URI, contentValues,
                SL_ContentProvider.KEY_ID + "= ?", new String[]{String.valueOf(getId())});

        addPriceChangeHistory(context);
    }

    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    private void addPriceChangeHistory(Context context) {
        /* Пока не будем добавлять кнопку "История цены", т.к. на мой взгяд этот график бесполезен
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_DATE, System.currentTimeMillis());
        contentValues.put(SL_ContentProvider.KEY_PRODUCT_ID, getId());
        contentValues.put(SL_ContentProvider.KEY_PRICE, getLastPrice());
        contentResolver.insert(SL_ContentProvider.PRICE_CHANGE_HISTORY_URI, contentValues);*/
    }

    @Override
    public void renameInDB(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(SL_ContentProvider.KEY_NAME, getName());
        contentResolver.update(SL_ContentProvider.PRODUCTS_CONTENT_URI,
                values, SL_ContentProvider.KEY_ID +  " = ?", new String[]{String.valueOf(getId())});
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public ITEM_TYPES getType(){
        return ITEM_TYPES.PRODUCT;
    }

    public void startProductActivity(Activity activity){
        Intent intent = new Intent(activity, ProductActivity.class);
        intent.putExtra(EXTRAS_KEYS.PRODUCT.getValue(), this);
        activity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
    }

    long getRowId() {
        return rowId;
    }

    @SuppressWarnings("unused")
    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}

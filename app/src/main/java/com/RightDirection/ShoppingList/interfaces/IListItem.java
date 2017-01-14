package com.RightDirection.ShoppingList.interfaces;

import android.net.Uri;
import android.os.Parcelable;

public interface IListItem extends Parcelable, IGetType {
    long getId();
    String getName();
    void setName(String name);
    void setImageUri(Uri imageUri);
    @SuppressWarnings("unused")
    void setChecked();
    @SuppressWarnings("unused")
    void setUnchecked();
    boolean isChecked();
    Uri getImageUri();
    @SuppressWarnings("unused")
    void setCount(float count);
    float getCount();
    @SuppressWarnings("unused")
    void setCount(String stringCount);
    void setId(long id);
}

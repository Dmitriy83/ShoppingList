package com.RightDirection.ShoppingList.interfaces;

import android.net.Uri;
import android.os.Parcelable;

public interface IListItem extends Parcelable, IGetType {
    long getId();
    String getName();
    void setName(String name);
    void setImageUri(Uri imageUri);
    void setChecked();
    void setUnchecked();
    boolean isChecked();
    Uri getImageUri();
    void setCount(float count);
    float getCount();
    void setCount(String stringCount);
    void setId(long id);
}

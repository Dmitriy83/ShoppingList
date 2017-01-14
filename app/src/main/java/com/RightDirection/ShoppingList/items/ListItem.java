package com.RightDirection.ShoppingList.items;

import android.net.Uri;
import android.os.Parcel;

import com.RightDirection.ShoppingList.enums.ITEM_TYPES;
import com.RightDirection.ShoppingList.interfaces.IListItem;

public class ListItem implements IListItem {

    private long id;
    private String name;
    boolean isChecked;
    private Uri imageUri;
    float count;
    public boolean isNew = false;

    ListItem(long id, String name) {
        this.id = id;
        this.name = name;
        this.imageUri = null;
        this.count = 1;
    }

    ListItem(long id, String name, Uri imageUri) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.count = 1;
    }

    ListItem(long id, String name, float count) {
        this.id = id;
        this.name = name;
        this.imageUri = null;
        this.count = count;
    }

    ListItem(Parcel in) {
        id = in.readLong();
        name = in.readString();
        isChecked = in.readByte() != 0;
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        count = in.readFloat();
        isNew = in.readByte() != 0;
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setChecked(){
        isChecked = true;
    }

    public void setUnchecked(){
        isChecked = false;
    }

    public boolean isChecked(){
        return isChecked;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeParcelable(imageUri, flags);
        dest.writeFloat(count);
        dest.writeByte((byte) (isNew ? 1 : 0));
    }

    public void setCount(float count) {
        if (count >= 0) {
            this.count = count;
        }
    }

    public float getCount() {
        return count;
    }

    public void setCount(String stringCount) {
        try {
            float count = Float.valueOf(stringCount);
            setCount(count);
        } catch (NumberFormatException e){
            // Не меняем количество
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public ITEM_TYPES getType() {
        return null;
    }
}

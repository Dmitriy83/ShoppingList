package com.RightDirection.ShoppingList;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ListItem implements Parcelable {

    private long id;
    private String name;
    private boolean checked;
    private Uri imageUri;
    private float count;

    public ListItem(long id, String name, Uri imageUri) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.count = 1;
    }

    public ListItem(long id, String name, Uri imageUri, float count) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.count = count;
    }

    protected ListItem(Parcel in) {
        id = in.readLong();
        name = in.readString();
        checked = in.readByte() != 0;
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        count = in.readFloat();
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
        checked = true;
    }

    public void setUnchecked(){
        checked = false;
    }

    public boolean isChecked(){
        return checked;
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
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeParcelable(imageUri, flags);
        dest.writeFloat(count);
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
}

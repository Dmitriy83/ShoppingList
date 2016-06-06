package com.RightDirection.ShoppingList;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ListItem implements Parcelable {

    private final String id;
    private String name;
    private boolean checked;
    private Uri imageUri;

    public ListItem(String id, String name, Uri imageUri) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
    }

    protected ListItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        checked = in.readByte() != 0;
        imageUri = in.readParcelable(Uri.class.getClassLoader());
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeParcelable(imageUri, flags);
    }
}

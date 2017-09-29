package com.RightDirection.ShoppingList.models;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.RightDirection.ShoppingList.activities.UnitActivity;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IDataBaseOperations;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.RightDirection.ShoppingList.utils.Utils;

/**
 * Класс "Единица измерения"
 */
public class Unit extends ListItem implements Parcelable, IDataBaseOperations {
    private String shortName;

    public Unit(long id, String name, String shortName) {
        super(id, name);
        this.shortName = shortName;
        if (id == 0){ this.id = Utils.EMPTY_ID; }
    }

    private Unit(Parcel in) {
        super(in);
        shortName = in.readString();
        if (id == 0){ this.id = Utils.EMPTY_ID; }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(shortName);
    }

    public static final Creator<Unit> CREATOR = new Creator<Unit>() {
        @Override
        public Unit createFromParcel(Parcel in) {
            return new Unit(in);
        }

        @Override
        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public void addToDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_UNIT_NAME, getName());
        contentValues.put(SL_ContentProvider.KEY_UNIT_SHORT_NAME, getShortName());
        Uri insertedId = contentResolver.insert(SL_ContentProvider.UNITS_CONTENT_URI, contentValues);
        setId(ContentUris.parseId(insertedId));
    }

    @Override
    public void removeFromDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(SL_ContentProvider.UNITS_CONTENT_URI, SL_ContentProvider.KEY_UNIT_ID + "= ?",
                new String[]{String.valueOf(getId())});
    }

    @Override
    public void updateInDB(Context context) {
        renameInDB(context);
    }

    @Override
    public void renameInDB(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SL_ContentProvider.KEY_UNIT_NAME, getName());
        contentValues.put(SL_ContentProvider.KEY_UNIT_SHORT_NAME, getShortName());
        contentResolver.update(SL_ContentProvider.UNITS_CONTENT_URI, contentValues,
                SL_ContentProvider.KEY_UNIT_ID + "= ?", new String[]{String.valueOf(getId())});
    }

    public void startUnitActivity(Activity activity){
        Intent intent = new Intent(activity, UnitActivity.class);
        intent.putExtra(EXTRAS_KEYS.UNIT.getValue(), this);
        activity.startActivityForResult(intent, Utils.NEED_TO_UPDATE);
    }
}

package com.RightDirection.ShoppingList.helpers;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.RightDirection.ShoppingList.interfaces.IOnClickItemListener;
import com.RightDirection.ShoppingList.ListItem;

import java.util.LinkedHashMap;
import java.util.List;

abstract public class ListAdapter extends ArrayAdapter<ListItem> {

    int mResource;
    Context mContext;
    LinkedHashMap mViewAndIdMatcher;

    public ListAdapter(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mViewAndIdMatcher = new LinkedHashMap();
    }
}

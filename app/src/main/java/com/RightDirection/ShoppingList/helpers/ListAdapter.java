package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.ListItem;

import java.util.LinkedHashMap;
import java.util.List;

abstract public class ListAdapter extends ArrayAdapter<ListItem> {

    int mResource;
    Context mContext;
    LinkedHashMap mViewAndIdMatcher;
    FragmentManager mFragmentManager = null;
    Activity mParentActivity;
    ListAdapter mListAdapter; // для доступа из обработичиков событий

    public ListAdapter(Context context, int resource, List<ListItem> objects, FragmentManager fragmentManager) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mViewAndIdMatcher = new LinkedHashMap();
        mFragmentManager = fragmentManager;
        mParentActivity = (Activity)context;
        mListAdapter = this;
    }

    public ListAdapter(Context context, int resource, List<ListItem> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mViewAndIdMatcher = new LinkedHashMap();
        mParentActivity = (Activity)context;
        mListAdapter = this;
    }
}

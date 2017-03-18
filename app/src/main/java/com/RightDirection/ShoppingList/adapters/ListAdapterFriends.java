package com.RightDirection.ShoppingList.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Category;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterFriends extends FirebaseRecyclerAdapter<User, ListAdapterFriends.ViewHolder> {
    private final Context mContext;

    public ListAdapterFriends(Context context) {
        super(User.class, R.layout.list_item_friends, ViewHolder.class, FirebaseUtil.getFriendsRef());
        mContext = context;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtName;
        public final ImageView imgFriendPhoto;
        public final ImageView imgDelete;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View rowView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(rowView);

            txtName = (TextView) rowView.findViewById(R.id.txtName);
            imgFriendPhoto = (ImageView) rowView.findViewById(R.id.imgFriendPhoto);
            imgDelete = (ImageView) rowView.findViewById(R.id.imgDelete);
            imgDelete.setTag(this);
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, User friend, int position) {
        // Set item views based on our views and data model
        viewHolder.txtName.setText(friend.getName());
        Picasso.with(mContext)
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.ic_person_outline)
                .fit()
                .into(viewHolder.imgFriendPhoto);

        viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder holder = (ViewHolder)view.getTag();
                getRef(holder.getAdapterPosition()).removeValue();
            }
        });
    }
}

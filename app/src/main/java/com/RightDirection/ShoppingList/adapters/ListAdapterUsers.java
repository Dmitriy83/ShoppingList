package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class ListAdapterUsers extends FirebaseRecyclerAdapter<User, ListAdapterUsers.ViewHolder> {
    private final Context mContext;

    public ListAdapterUsers(Context context, DatabaseReference tableRef) {
        super(User.class, R.layout.list_item_users, ViewHolder.class, tableRef);
        mContext = context;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtName;
        final ImageView imgUserPhoto;
        public final ImageView imgDelete;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View rowView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(rowView);

            txtName = rowView.findViewById(R.id.txtName);
            imgUserPhoto = rowView.findViewById(R.id.imgFriendPhoto);
            imgDelete = rowView.findViewById(R.id.imgDelete);
            imgDelete.setTag(this);
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, User friend, int position) {
        // Set item views based on our views and data model
        viewHolder.txtName.setText(friend.getName());
        Glide.with(mContext)
                .load(friend.getPhotoUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_person_outline)
                        .fitCenter()
                        .dontAnimate()
                        .dontTransform())
                .into(viewHolder.imgUserPhoto);

        viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder holder = (ViewHolder)view.getTag();
                getRef(holder.getAdapterPosition()).removeValue();
            }
        });
    }
}

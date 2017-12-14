package com.RightDirection.ShoppingList.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

public class ListAdapterRecipients extends FirebaseRecyclerAdapter<User, ListAdapterRecipients.ViewHolder> {
    private final Context mContext;

    public ListAdapterRecipients(Context context) {
        super(User.class, R.layout.list_item_choose_recipient, ViewHolder.class, FirebaseUtil.getFriendsRef());
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final ImageView imgFriendPhoto;
        final RelativeLayout productRepresent;

        public ViewHolder(View rowView) {
            super(rowView);
            txtName = rowView.findViewById(R.id.txtName);
            imgFriendPhoto = rowView.findViewById(R.id.imgFriendPhoto);
            productRepresent = rowView.findViewById(R.id.itemRepresent);
            assert productRepresent != null;
            productRepresent.setTag(this);
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, User friend, int position) {
        viewHolder.txtName.setText(friend.getName());
        Picasso.with(mContext)
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.ic_person_outline)
                .fit()
                .into(viewHolder.imgFriendPhoto);
        viewHolder.productRepresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListAdapterRecipients.ViewHolder holder = (ListAdapterRecipients.ViewHolder)view.getTag();
                String chosenKey = getRef(holder.getAdapterPosition()).getKey();
                try {
                    IOnRecipientChosenListener onRecipientChosenListener = (IOnRecipientChosenListener) mContext;
                    onRecipientChosenListener.onRecipientChosen(chosenKey);
                } catch (ClassCastException e) {
                    throw new ClassCastException(mContext.toString() + " должна поддерживать итерфейс IOnNewItemAddedListener");
                }
            }
        });
    }

    public interface IOnRecipientChosenListener{void onRecipientChosen(String key);}
}



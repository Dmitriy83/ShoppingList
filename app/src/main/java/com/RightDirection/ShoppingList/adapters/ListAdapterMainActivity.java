package com.RightDirection.ShoppingList.adapters;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.activities.InputNameDialog;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;
import com.RightDirection.ShoppingList.items.ShoppingList;

import java.util.ArrayList;

public class ListAdapterMainActivity extends ListAdapter {

    private ActionMode mActionMode;
    private ShoppingList mSelectedItem = null;
    private View mSelectedView;

    public ListAdapterMainActivity(Context context, int resource, ArrayList<ShoppingList> objects) {
        super(context, resource, objects);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.productNameView.setOnClickListener(onProductNameViewClick);
        viewHolder.productNameView.setOnLongClickListener(onProductNameViewLongClick);
    }

    private final View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedView = view;
            mSelectedView.setSelected(true);

            ShoppingList item = (ShoppingList) view.getTag();
            Intent intent = new Intent(mParentActivity, ShoppingListInShopActivity.class);
            intent.putExtra(String.valueOf(R.string.list_id), item.getId());
            intent.putExtra(String.valueOf(R.string.list_name), item.getName());
            ActivityCompat.startActivity(mParentActivity, intent, null);
        }
    };

    private final View.OnLongClickListener onProductNameViewLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            mSelectedItem = (ShoppingList) view.getTag();

            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedView = view;
            mSelectedView.setSelected(true);

            // Start the CAB using the ActionMode.Callback defined above
            //mActionMode = mParentActivity.startActionMode(mActionModeCallback);
            if (mActionMode == null) {
                Toolbar toolbar = (Toolbar) mParentActivity.findViewById(R.id.toolbar);
                if (toolbar != null) mActionMode = toolbar.startActionMode(mActionModeCallback);
            }
            return true;
        }
    };

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activity_main_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            // Откроем список для редактирования
            if (mSelectedItem == null) { return false; }

            switch (item.getItemId()) {
                case R.id.imgDelete:

                    // Выведем вопрос об удалении списка покупок
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            new ContextThemeWrapper(mParentActivity, mParentActivity.getApplicationInfo().theme)).create();

                    alertDialog.setMessage(mParentActivity.getString(R.string.delete_shopping_list_question));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mParentActivity.getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    // Удалим запись из БД по id
                                    mSelectedItem.removeFromDB(mParentActivity);

                                    // Обновим списки покупок
                                    remove(mSelectedItem);

                                    mActionMode.finish(); // Action picked, so close the CAB
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mParentActivity.getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { mActionMode.finish(); }
                            });

                    alertDialog.show();

                    return true;

                case R.id.imgEdit:

                    Intent intent = new Intent(mParentActivity.getBaseContext(), ShoppingListEditingActivity.class);
                    intent.putExtra(String.valueOf(R.string.is_new_list), false);
                    intent.putExtra(String.valueOf(R.string.list_id), mSelectedItem.getId());
                    intent.putExtra(String.valueOf(R.string.list_name), mSelectedItem.getName());
                    mParentActivity.startActivity(intent);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.imgChangeListName:

                    // Откроем окно для ввода нового наименования списка/
                    // Сохранение будет производиться в методе onDialogPositiveClick
                    InputNameDialog inputNameDialog = new InputNameDialog();
                    inputNameDialog.setInitName(mSelectedItem.getName());
                    inputNameDialog.setId(mSelectedItem.getId());
                    FragmentManager fragmentManager = mParentActivity.getFragmentManager();
                    inputNameDialog.show(fragmentManager, null);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.imgSendListByEmail:
                    mSelectedItem.sendByEmail(mParentActivity);
                    mode.finish(); // Action picked, so close the CAB
                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

}

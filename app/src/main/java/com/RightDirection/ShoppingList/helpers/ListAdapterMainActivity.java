package com.RightDirection.ShoppingList.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.ShoppingList;
import com.RightDirection.ShoppingList.activities.InputListNameDialog;
import com.RightDirection.ShoppingList.activities.ShoppingListEditingActivity;
import com.RightDirection.ShoppingList.activities.ShoppingListInShopActivity;

import java.util.ArrayList;

public class ListAdapterMainActivity extends ListAdapter {

    private ActionMode mActionMode;
    private ShoppingList mSelectedItem = null;
    private View mSelectedView;

    public ListAdapterMainActivity(Context context, int resource, ArrayList<ShoppingList> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewInitializer viewInitializer = new ViewInitializer(position, convertView);

        viewInitializer.viewHolder.productNameView.setOnClickListener(onProductNameViewClick);
        viewInitializer.viewHolder.productNameView.setOnLongClickListener(onProductNameViewLongClick);

        return viewInitializer.rowView;
    }

    private final View.OnClickListener onProductNameViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mSelectedView != null) mSelectedView.setSelected(false);
            mSelectedView = view;
            mSelectedView.setSelected(true);

            ShoppingList item = (ShoppingList) view.getTag();
            Activity parentActivity = (Activity) mContext;
            Intent intent = new Intent(parentActivity, ShoppingListInShopActivity.class);
            intent.putExtra(String.valueOf(R.string.list_id), item.getId());
            ActivityCompat.startActivity(parentActivity, intent, null);
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
                                    notifyDataSetChanged();

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
                    mParentActivity.startActivity(intent);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.imgChangeListName:

                    // Откроем окно для ввода нового наименования списка/
                    // Сохранение будет производиться в методе onDialogPositiveClick
                    InputListNameDialog inputListNameDialog = new InputListNameDialog();
                    inputListNameDialog.setInitName(mSelectedItem.getName());
                    inputListNameDialog.setId(mSelectedItem.getId());
                    FragmentManager fragmentManager = mParentActivity.getFragmentManager();
                    inputListNameDialog.show(fragmentManager, null);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.imgSendListByEmail:

                    try{
                        // Создадим JSON файл по списку покупок
                        String fileName = mParentActivity.getString(R.string.json_file_identifier) + " '" + mSelectedItem.getName() + "'" + ".json";
                        Utils.createShoppingListJSONFile(getContext(), mSelectedItem, fileName);

                        mParentActivity.startActivity(Utils.getSendEmailIntent("d.zhiharev@mail.ru",
                                mParentActivity.getString(R.string.json_file_identifier)
                                        + " '" + mSelectedItem.getName() + "'", "", fileName));
                    }
                    catch(Exception e){
                        System.out.println("Exception raises during sending mail. Discription: " + e);
                    }

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

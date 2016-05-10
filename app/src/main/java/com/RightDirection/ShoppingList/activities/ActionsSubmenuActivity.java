package com.RightDirection.ShoppingList.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;

public class ActionsSubmenuActivity extends Activity {

    private int mY;
    private Context mContextForDialog;

    public static ListAdapter mCallingActivityAdapter;
    public static ListItem mListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_actions_submenu);

        mContextForDialog = this;

        // Получим значения из переданных параметров
        Intent sourceIntent = getIntent();
        mY = sourceIntent.getIntExtra("y", 0);

        // Добавим обработчики кликов по кнопкам
        ImageButton imgEdit = (ImageButton) findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(onImgEditClick);

        ImageButton imgDelete = (ImageButton) findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(onImgDeleteClick);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        View view = getWindow().getDecorView();
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) view.getLayoutParams();

        // Отключим затемнение заднего фона
        layoutParams.dimAmount=0.0f;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        // Отобразим в нужной позиции
        layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        layoutParams.y = mY;
        getWindowManager().updateViewLayout(view, layoutParams);
    }

    private View.OnClickListener onImgEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                    null, "_id = " + mListItem.getId(), null, null);
            if (cursor.moveToFirst()) {
                Intent intent = new Intent(getBaseContext(), ShoppingListEditingActivity.class);
                intent.putExtra(String.valueOf(R.string.is_new_list), false);
                String itemId = cursor.getString(cursor.getColumnIndex(ShoppingListContentProvider.KEY_ID));
                intent.putExtra(String.valueOf(R.string.list_id), itemId);
                startActivity(intent);
            }
            cursor.close();
            finish();
        }
    };

    private View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Выведем вопрос об удалении списка покупок
            AlertDialog alertDialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(mContextForDialog, getApplicationInfo().theme)).create();

            alertDialog.setMessage(getString(R.string.delete_shopping_list_question));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // Удалим запись из БД по id
                            ContentResolver contentResolver = getContentResolver();
                            contentResolver.delete(ShoppingListContentProvider.SHOPPING_LIST_CONTENT_CONTENT_URI,
                                    ShoppingListContentProvider.KEY_SHOPPING_LIST_ID + "=" + mListItem.getId(), null);
                            contentResolver.delete(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                                    ShoppingListContentProvider.KEY_ID + "=" + mListItem.getId(), null);

                            // Обновим списки покупок
                            mCallingActivityAdapter.remove(mListItem);
                            mCallingActivityAdapter.notifyDataSetChanged();

                            finish();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            alertDialog.show();
            setVisible(false); // Чтобы избежать "мигания" перед закрытием
        }
    };
}

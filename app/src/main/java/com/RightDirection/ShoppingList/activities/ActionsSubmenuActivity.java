package com.RightDirection.ShoppingList.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.RightDirection.ShoppingList.ListItem;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.helpers.ShoppingListContentProvider;

public class ActionsSubmenuActivity extends Activity implements InputListNameDialog.IInputListNameDialogListener {

    private int mY;
    private Context mContextForDialog;
    private ListItem mListItem;

    public static ListAdapter mCallingActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_actions_submenu);

        mContextForDialog = this;

        // Получим значения из переданных параметров
        Intent sourceIntent = getIntent();
        mY = sourceIntent.getIntExtra("y", 0);
        mListItem = sourceIntent.getParcelableExtra(String.valueOf(R.string.list_item));

        // Добавим обработчики кликов по кнопкам
        ImageButton imgSendListByEmail = (ImageButton) findViewById(R.id.imgSendListByEmail);
        imgSendListByEmail.setOnClickListener(onImgSendListByEmailClick);

        ImageButton imgChangeListName = (ImageButton) findViewById(R.id.imgChangeListName);
        imgChangeListName.setOnClickListener(onImgChangeListNameClick);

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

        // Отключим затемнение фона вокруг окна
        layoutParams.dimAmount=0.0f;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        // Отобразим в нужной позиции
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.y = mY;
        getWindowManager().updateViewLayout(view, layoutParams);
    }

    private final View.OnClickListener onImgSendListByEmailClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Создадим письмо с прикрепленным файлом сериализованного списка покупок и откроем его

            finish();
        }
    };

    private final View.OnClickListener onImgChangeListNameClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Откроем окно для ввода нового наименования списка/
            // Сохранение будет производиться в методе onDialogPositiveClick
            InputListNameDialog inputListNameDialog = new InputListNameDialog();
            inputListNameDialog.setInitName(mListItem.getName());
            FragmentManager fragmentManager = getFragmentManager();
            inputListNameDialog.show(fragmentManager, null);
        }
    };

    private final View.OnClickListener onImgEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(), ShoppingListEditingActivity.class);
            intent.putExtra(String.valueOf(R.string.is_new_list), false);
            intent.putExtra(String.valueOf(R.string.list_id), mListItem.getId());
            startActivity(intent);

            finish();
        }
    };

    private final View.OnClickListener onImgDeleteClick = new View.OnClickListener() {
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

    @Override
    public void onDialogPositiveClick(String listName) {
        mListItem.setName(listName);

        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ShoppingListContentProvider.KEY_NAME, mListItem.getName());
        contentResolver.update(ShoppingListContentProvider.SHOPPING_LISTS_CONTENT_URI,
                values, ShoppingListContentProvider.KEY_ID +  " = " + mListItem.getId(), null);

        finish();
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
    }
}

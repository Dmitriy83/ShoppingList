package com.RightDirection.ShoppingList.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;

public class InputNameDialog extends DialogFragment{

    private final boolean mIsProduct = false;
    private String mInitName = "";
    private long mId;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface IInputListNameDialogListener {
        void onDialogPositiveClick(String listName, long listId, boolean isProduct);
        @SuppressWarnings("EmptyMethod")
        void onDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private IInputListNameDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Используем класс-конструктор для удобного построения окна диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), getActivity().getApplicationInfo().theme));
        // Получим "заполнитель" макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Заполним и привяжем макет к диалогу
        // Передадим null в качестве родительского view, т.к. заполнение будет сделано в макете диалога
        // В соответствии с https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        // случай с диалогом - это исключение из правил и предупреждение при синтаксическом контроле можно игнорировать
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.input_new_list_name_dialog, null);
        final EditText inputNewListName = (EditText)view.findViewById(R.id.inputNewListName);
        inputNewListName.setText(mInitName);

        builder.setView(view);

        // Добавим кнопки-действия
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(inputNewListName.getText().toString(), mId, mIsProduct);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the InputListNameDialogListener
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InputListNameDialogListener so we can send events to the host
            mListener = (IInputListNameDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement InputListNameDialogListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InputListNameDialogListener so we can send events to the host
            mListener = (IInputListNameDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement InputListNameDialogListener");
        }
    }

    public void setInitName(String name){
        mInitName = name;
    }

    public void setId(long id) {
        mId = id;
    }
}

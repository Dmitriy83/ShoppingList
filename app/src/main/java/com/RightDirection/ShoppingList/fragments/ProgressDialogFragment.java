package com.RightDirection.ShoppingList.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    private static final String KEY_TITLE = "title";

    public static ProgressDialogFragment newInstance(String text) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        frag.setCancelable(false);
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, text);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        Bundle arguments = getArguments();
        if (arguments != null) {
            dialog.setMessage(arguments.getString(KEY_TITLE));
        }
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
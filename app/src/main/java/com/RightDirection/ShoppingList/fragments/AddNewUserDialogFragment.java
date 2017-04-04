package com.RightDirection.ShoppingList.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.utils.FirebaseObservables;
import com.RightDirection.ShoppingList.utils.Utils;
import java.util.concurrent.TimeUnit;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class AddNewUserDialogFragment extends DialogFragment {
    private String TAG = "AddNewUserFragment";

    public static AddNewUserDialogFragment newInstance(User user) {
        AddNewUserDialogFragment f = new AddNewUserDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(EXTRAS_KEYS.AUTHOR.getValue(), user);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final User user = getArguments().getParcelable(EXTRAS_KEYS.AUTHOR.getValue());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String userName = (user == null ? "unknown" : user.getName());
        builder.setMessage(getString(R.string.new_user_sent_list, userName));
        builder.setPositiveButton(getString(R.string.add_to_friends_and_receive_list), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseObservables.addFriendObservable(user)
                        .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean success) throws Exception {
                                        if (success) {
                                            Log.d(TAG, "New friend was added");
                                        } else {
                                            Log.e(TAG, "User can not be added to friends");
                                        }
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable e) throws Exception {
                                        Log.e(TAG, e.getMessage());
                                    }
                                });
            }
        });
        builder.setNegativeButton(getString(R.string.add_to_black_list_and_decline_list), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseObservables.addUserToBlackListObservable(user)
                        .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean success) throws Exception {
                                        if (success) {
                                            Log.d(TAG, "User was added to black list");
                                        } else {
                                            Log.e(TAG, "User can not be added to black list");
                                        }
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable e) throws Exception {
                                        Log.e(TAG, e.getMessage());
                                    }
                                });
            }
        });
        return builder.create();
    }
}

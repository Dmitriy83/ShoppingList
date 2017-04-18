package com.RightDirection.ShoppingList.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.fragments.AddNewUserDialogFragment;
import com.RightDirection.ShoppingList.fragments.ProgressDialogFragment;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.utils.Utils;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_FRAGMENT = "tagDialogFragment";
    private BroadcastReceiver mServiceReceiver;

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter serviceActiveFilter = new IntentFilter();
        serviceActiveFilter.addAction(Utils.ACTION_NOTIFICATION);
        serviceActiveFilter.addAction(Utils.ACTION_ADD_USER_TO_FRIENDS);
        mServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;

                if (intent.getAction().equals(Utils.ACTION_NOTIFICATION)){
                    String msg = intent.getStringExtra(EXTRAS_KEYS.NOTIFICATION.getValue());
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }else if (intent.getAction().equals(Utils.ACTION_ADD_USER_TO_FRIENDS)){
                    User user = intent.getParcelableExtra(EXTRAS_KEYS.AUTHOR.getValue());
                    AddNewUserDialogFragment dialog = AddNewUserDialogFragment.newInstance(user);
                    dialog.show(getFragmentManager(), dialog.getTag());
                }
            }
        };
        this.registerReceiver(mServiceReceiver, serviceActiveFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mServiceReceiver);
    }

    void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev == null) {
            ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(message);
            fragment.show(ft, TAG_DIALOG_FRAGMENT);
        }
    }

    void dismissProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev != null) {
            ft.remove(prev).commit();
        }
    }

    private Fragment getExistingDialogFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENT);
    }
}
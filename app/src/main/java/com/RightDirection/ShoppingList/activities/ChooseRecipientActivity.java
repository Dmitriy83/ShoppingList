package com.RightDirection.ShoppingList.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterRecipients;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.interfaces.IListItem;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.TimeoutControl;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseRecipientActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener, ListAdapterRecipients.IOnRecipientChosenListener{

    private static final String TAG = "ChooseRecipientActivity";
    private ListAdapterRecipients mRecipientsAdapter;
    private ShoppingList mShoppingList;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_recipient);

        mShoppingList = getIntent().getParcelableExtra(EXTRAS_KEYS.SHOPPING_LIST.getValue());
        if (mShoppingList != null){
            ArrayList<IListItem> products = getIntent().getParcelableArrayListExtra(EXTRAS_KEYS.PRODUCTS.getValue());
            if (products != null) mShoppingList.setProducts(products);
        }else {
            FirebaseCrash.logcat(Log.ERROR, TAG, getString(R.string.shopping_list_transfer_error));
            Toast.makeText(this, R.string.shopping_list_transfer_error, Toast.LENGTH_LONG).show();
            finish();
        }

        FloatingActionButton fabAddNewFriends = (FloatingActionButton) findViewById(R.id.fabAddNewFriends);
        assert fabAddNewFriends != null;
        fabAddNewFriends.setOnClickListener(fabAddNewFriendsOnClickListener);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvRecipients);
        if (emptyView != null && recyclerView != null) recyclerView.setEmptyView(emptyView);
    }

    private final Button.OnClickListener fabAddNewFriendsOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Обновление списка производим при каждом рестарте активности
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvRecipients);
        assert recyclerView != null;
        showRecyclerViewProgressBar();
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecipientsAdapter = new ListAdapterRecipients(this);
        recyclerView.setAdapter(mRecipientsAdapter);

        // Обработаем таймаут
        final TimeoutControl timeoutControl = new TimeoutControl(Utils.TIMEOUT);
        timeoutControl.addListener(new TimeoutControl.IOnTimeoutListener() {
            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Уберем ProgressBar и изменим надпись в RecyclerView
                        hideRecyclerViewProgressBar();
                        TextView emptyView = (TextView) findViewById(R.id.empty_view);
                        assert emptyView != null;
                        emptyView.setText(R.string.connection_timeout_exceeded);
                    }
                });
            }
        });
        timeoutControl.start();

        // Добавим слушателя на событие, которое будет вызываться один раз.
        // В данном случае, при окончании первоначальной загрузки данных в адаптер.
        FirebaseUtil.getBaseRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timeoutControl.stop();
                hideRecyclerViewProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                timeoutControl.stop();
                hideRecyclerViewProgressBar();
                System.out.println(R.string.connection_failed + " " + databaseError.getCode());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Очистим адаптер, что прекратит чтение из Firebase
        mRecipientsAdapter.cleanup();
    }

    private void showRecyclerViewProgressBar(){
        FrameLayout pb = (FrameLayout)findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerViewProgressBar(){
        FrameLayout pb = (FrameLayout)findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.GONE);
    }

    @Override
    public void onRecipientChosen(String userKey) {
        showProgressDialog(getString(R.string.sending));

        // Обработаем таймаут
        final TimeoutControl timeoutControl = new TimeoutControl(Utils.TIMEOUT);
        timeoutControl.addListener(new TimeoutControl.IOnTimeoutListener() {
            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        Toast.makeText(getApplicationContext(), R.string.connection_timeout_exceeded, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        timeoutControl.start();

        // Отправим список покупок в FireBase
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("content", mShoppingList.convertShoppingListToString(this));
        FirebaseUtil.getUsersRef().child(userKey).child(FirebaseUtil.getShoppingListsPath())
                .child(mShoppingList.getName()).updateChildren(updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        timeoutControl.stop();
                        dismissProgressDialog();
                        if (firebaseError != null) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.error_savin_user_data) + firebaseError.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.sent_successfully),
                                    Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                });
    }
}

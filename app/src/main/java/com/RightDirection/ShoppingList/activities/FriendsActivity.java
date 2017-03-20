package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterFriends;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.TimeoutControl;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "FriendsActivity";
    private ListAdapterFriends mFriendsAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setTitle(R.string.action_friends);

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnSearchFriend = (Button) findViewById(R.id.btnSearchFriend);
        if (btnSearchFriend != null) {
            btnSearchFriend.setOnClickListener(btnSearchFriendOnClickListener);
        }

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvFriends);
        if (emptyView != null && recyclerView != null) recyclerView.setEmptyView(emptyView);
    }

    private final Button.OnClickListener btnSearchFriendOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText etFriendEmail = (EditText) findViewById(R.id.etFriendEmail);
            assert etFriendEmail != null;
            showProgressDialog(getString(R.string.searching));
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
            FirebaseUtil.getUsersRef().orderByChild(FirebaseUtil.getEmailKey())
                    .equalTo(etFriendEmail.getText().toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            timeoutControl.stop();
                            dismissProgressDialog();
                            if (!dataSnapshot.hasChildren()) {
                                Toast.makeText(getApplicationContext(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
                            } else {
                                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                    User newFriend = childDataSnapshot.getValue(User.class);
                                    newFriend.setUid(childDataSnapshot.getKey());
                                    // Добавим в список друзей в FireBase
                                    Map<String, Object> updateValues = new HashMap<>();
                                    updateValues.put("name", newFriend.getName());
                                    updateValues.put("photoUrl", newFriend.getPhotoUrl());
                                    updateValues.put("userEmail", newFriend.getUserEmail());
                                    FirebaseUtil.getFriendsRef().child(newFriend.getUid()).updateChildren(updateValues,
                                            new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                                    if (firebaseError != null) {
                                                        Toast.makeText(FriendsActivity.this,
                                                                "Couldn't save user data: " + firebaseError.getMessage(),
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            timeoutControl.stop();
                            dismissProgressDialog();
                            System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                            Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
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
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvFriends);
        assert recyclerView != null;
        showRecyclerViewProgressBar();
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFriendsAdapter = new ListAdapterFriends(this);
        recyclerView.setAdapter(mFriendsAdapter);

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
        mFriendsAdapter.cleanup();
    }

    private void showRecyclerViewProgressBar(){
        FrameLayout pb = (FrameLayout)findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerViewProgressBar(){
        FrameLayout pb = (FrameLayout)findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.GONE);
    }
}

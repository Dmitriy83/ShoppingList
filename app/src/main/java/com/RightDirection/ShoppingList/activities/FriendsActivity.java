package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterUsers;
import com.RightDirection.ShoppingList.utils.FirebaseObservables;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.RightDirection.ShoppingList.views.NpaLinearLayoutManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class FriendsActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "FriendsActivity";
    private ListAdapterUsers mFriendsAdapter;

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
        CustomRecyclerView recyclerView = (CustomRecyclerView) findViewById(R.id.rvFriends);
        if (emptyView != null && recyclerView != null) recyclerView.setEmptyView(emptyView);
    }

    private final Button.OnClickListener btnSearchFriendOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText etFriendEmail = (EditText) findViewById(R.id.etFriendEmail);
            assert etFriendEmail != null;
            showProgressDialog(getString(R.string.searching));
            FirebaseObservables.addFriendsByEmailObservable(etFriendEmail.getText().toString())
                    .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean success) throws Exception {
                                    dismissProgressDialog();
                                    if (!success){
                                        Toast.makeText(getApplicationContext(),
                                                R.string.user_not_found,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable e) throws Exception {
                                    dismissProgressDialog();
                                    if (e instanceof TimeoutException) {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.connection_timeout_exceeded,
                                                Toast.LENGTH_LONG).show();
                                    } else{
                                        Toast.makeText(FriendsActivity.this,
                                                getString(R.string.error_saving_user_data)
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
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
        CustomRecyclerView recyclerView = (CustomRecyclerView) findViewById(R.id.rvFriends);
        assert recyclerView != null;
        showRecyclerViewProgressBar();
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(this));
        mFriendsAdapter = new ListAdapterUsers(this, FirebaseUtil.getFriendsRef());
        recyclerView.setAdapter(mFriendsAdapter);

        // Добавим слушателя на событие, которое будет вызываться один раз.
        // В данном случае, при окончании первоначальной загрузки данных в адаптер.
        FirebaseObservables.endLoadingToAdapterObservable()
                .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) throws Exception {
                                hideRecyclerViewProgressBar();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable e) throws Exception {
                                hideRecyclerViewProgressBar();
                                if (e instanceof TimeoutException) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.connection_timeout_exceeded,
                                            Toast.LENGTH_LONG).show();
                                    TextView emptyView = (TextView) findViewById(R.id.empty_view);
                                    assert emptyView != null;
                                    emptyView.setText(R.string.connection_timeout_exceeded);
                                } else{
                                    System.out.println(R.string.connection_failed + " " + e.getMessage());
                                }
                            }
                        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Очистим адаптер, что прекратит чтение из Firebase
        mFriendsAdapter.cleanup();
    }

    private void showRecyclerViewProgressBar() {
        FrameLayout pb = (FrameLayout) findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerViewProgressBar() {
        FrameLayout pb = (FrameLayout) findViewById(R.id.frameProgressBar);
        pb.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                super.onBackPressed();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

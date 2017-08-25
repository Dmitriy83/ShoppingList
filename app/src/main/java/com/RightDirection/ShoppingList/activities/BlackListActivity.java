package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.adapters.ListAdapterUsers;
import com.RightDirection.ShoppingList.utils.FirebaseObservables;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.RightDirection.ShoppingList.utils.Utils;
import com.RightDirection.ShoppingList.views.CustomRecyclerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class BlackListActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "BlackListActivity";
    private ListAdapterUsers mBlackListAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);

        setTitle(R.string.action_black_list);

        // Подключим меню
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Добавим текстовое поле для пустого списка
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvBlackList);
        assert recyclerView != null; assert emptyView != null;
        recyclerView.setEmptyView(emptyView);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Обновление списка производим при каждом рестарте активности
        CustomRecyclerView recyclerView = (CustomRecyclerView)findViewById(R.id.rvBlackList);
        assert recyclerView != null;
        showRecyclerViewProgressBar();
        // Используем этот метод для увеличения производительности,
        // т.к. содержимое не изменяет размер макета
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBlackListAdapter = new ListAdapterUsers(this, FirebaseUtil.getBlackListRef());
        recyclerView.setAdapter(mBlackListAdapter);

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

        /*FirebaseUtil.getBaseRef().child(".info/connected/")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue(Boolean.class)) {
                            Log.i(TAG, "Firebase CONNECTED");
                        } else {
                            Log.i(TAG, "Firebase NOT CONNECTED");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "onCancelled: ", error.toException());
                    }
                });*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Очистим адаптер, чтобы прекратит чтение из Firebase
        mBlackListAdapter.cleanup();
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

package com.RightDirection.ShoppingList.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.models.User;
import com.RightDirection.ShoppingList.services.AlarmReceiver;
import com.RightDirection.ShoppingList.utils.FirebaseObservables;
import com.RightDirection.ShoppingList.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.RightDirection.ShoppingList.utils.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class ProfileActivity extends BaseActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ProfileActivity";
    private ViewGroup mProfileUi;
    private ViewGroup mSignInUi;
    private FirebaseAuth mAuth;
    private CircleImageView mProfilePhoto;
    private TextView mProfileUsername;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 103;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Подключим меню
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Добавим кнопку Up на toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.authentication);

        // Initialize authentication and set up callbacks
        mAuth = FirebaseAuth.getInstance();

        // GoogleApiClient with Sign In
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .build())
                .build();

        mSignInUi = findViewById(R.id.sign_in_ui);
        mProfileUi = findViewById(R.id.profile);

        mProfilePhoto = findViewById(R.id.profile_user_photo);
        mProfileUsername = findViewById(R.id.profile_user_name);

        findViewById(R.id.launch_sign_in).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.launch_sign_in:
                launchSignInIntent();
                break;
            case R.id.sign_out_button:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                cancelReceiveShoppingListsAlarm();

                showSignedOutUI();
                break;
        }
    }

    private void launchSignInIntent() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.getStatus());
        if (result.isSuccess()) {
            // Successful Google sign in, authenticate with Firebase.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            // Unsuccessful Google Sign In, show signed-out UI
            Log.d(TAG, "Google Sign-In failed.");
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        showProgressDialog(getString(R.string.profile_progress_message));

        FirebaseObservables.signInObservable(mAuth, credential)
                .timeout(Utils.TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<AuthResult>() {
                            @Override
                            public void accept(AuthResult result) throws Exception {
                                handleFirebaseAuthResult(result);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable e) throws Exception {
                                if (e instanceof TimeoutException) {
                                    Toast.makeText(getApplicationContext(), R.string.connection_timeout_exceeded,
                                            Toast.LENGTH_LONG).show();
                                } else{
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.authentication_failed) + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                                handleFirebaseAuthResult(null);
                            }
                        });
    }

    private void handleFirebaseAuthResult(AuthResult result) {
        dismissProgressDialog();
        if (result != null) {
            showSignedInUI(result.getUser());
        } else {
            showSignedOutUI();
        }
    }
    private void showSignedInUI(FirebaseUser firebaseUser) {
        Log.d(TAG, "Showing signed in UI");
        mSignInUi.setVisibility(View.GONE);
        mProfileUi.setVisibility(View.VISIBLE);
        mProfileUsername.setVisibility(View.VISIBLE);
        mProfilePhoto.setVisibility(View.VISIBLE);
        if (firebaseUser.getDisplayName() != null) {
            mProfileUsername.setText(firebaseUser.getDisplayName());
        }

        if (firebaseUser.getPhotoUrl() != null) {
            loadProfileIcon(firebaseUser.getPhotoUrl().toString());
        }
        Map<String, Object> updateValues = new HashMap<>();
        String userName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Anonymous";
        updateValues.put("name", userName);
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;
        updateValues.put("photoUrl", photoUrl);
        // По адроесу эл. почты будет осуществляться поиск друзей в базе
        String userEmail = firebaseUser.getEmail();
        updateValues.put(FirebaseUtil.EMAIL_KEY, userEmail);

        FirebaseUtil.getUsersRef().child(firebaseUser.getUid()).updateChildren(
                updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        if (firebaseError != null) {
                            Toast.makeText(ProfileActivity.this,
                                    "Couldn't save user data: " + firebaseError.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Сохраним текущего пользователя в настройках, чтобы отображать информацию о нем,
        // даже если соединение с интернет отсутствует
        FirebaseUtil.writeUserToPref(this, new User(firebaseUser.getUid(), userName, photoUrl));
    }

    private void loadProfileIcon(String url) {
        Picasso.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person_outline)
                .fit()
                .into(mProfilePhoto);
    }

    private void showSignedOutUI() {
        Log.d(TAG, "Showing signed out UI");
        mProfileUsername.setText("");
        mSignInUi.setVisibility(View.VISIBLE);
        mProfileUi.setVisibility(View.GONE);

        // Очистим информацию о пользователе из настроек
        FirebaseUtil.removeUserFromPref(this);
    }

    private void cancelReceiveShoppingListsAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            alarm.cancel(pIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            dismissProgressDialog();
            showSignedInUI(currentUser);
        } else {
            showSignedOutUI();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }
}

package com.RightDirection.ShoppingList.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.models.FirebaseShoppingList;
import com.RightDirection.ShoppingList.models.ShoppingList;
import com.RightDirection.ShoppingList.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class FirebaseObservables {
    public static Observable<ArrayList<User>> friendsObservable() {
        return Observable.create(new ObservableOnSubscribe<ArrayList<User>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<User>> emitter) throws Exception {
                addFBListenerToReceiveFriends(emitter);
            }
        });
    }

    public static Observable<ArrayList<User>> blackListsObservable() {
        return Observable.create(new ObservableOnSubscribe<ArrayList<User>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<User>> emitter) throws Exception {
                addFBListenerToReceiveBlackList(emitter);
            }
        });
    }

    public static Observable<ArrayList<FirebaseShoppingList>> shoppingListsObservable() {
        return Observable.create(new ObservableOnSubscribe<ArrayList<FirebaseShoppingList>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<FirebaseShoppingList>> emitter) throws Exception {
                addFBListenerToReceiveShoppingLists(emitter);
            }
        });
    }

    private static void addFBListenerToReceiveFriends(final ObservableEmitter<ArrayList<User>> emitter) {
        DatabaseReference friendsRef = FirebaseUtil.getFriendsRef();
        if (friendsRef == null) {
            return;
        }

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                emitter.onNext(receiveFriendsFromFirebase(dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private static void addFBListenerToReceiveBlackList(final ObservableEmitter<ArrayList<User>> emitter) {
        DatabaseReference blackListRef = FirebaseUtil.getBlackListRef();
        if (blackListRef == null) {
            return;
        }

        blackListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                emitter.onNext(receiveBlackListFromFirebase(dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private static void addFBListenerToReceiveShoppingLists(final ObservableEmitter<ArrayList<FirebaseShoppingList>> emitter) {
        DatabaseReference shoppingListsRef = FirebaseUtil.getShoppingListsRef();
        if (shoppingListsRef == null) {
            return;
        }

        shoppingListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                emitter.onNext(FirebaseUtil.getShoppingListsFromFB(dataSnapshot));
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private static ArrayList<User> receiveFriendsFromFirebase(DataSnapshot dataSnapshot) {
        return receiveUsersFromFirebase(dataSnapshot);
    }

    private static ArrayList<User> receiveBlackListFromFirebase(DataSnapshot dataSnapshot) {
        ArrayList<User> blackList = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            User user = childDataSnapshot.getValue(User.class);
            user.setUid(childDataSnapshot.getKey());
            blackList.add(user);
        }
        return blackList;
    }

    public static Observable<Boolean> addFriendsByEmailObservable(final String email) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                addFBListenerToAddFriendsByEmail(emitter, email);
            }
        });
    }

    private static void addFBListenerToAddFriendsByEmail(final ObservableEmitter<Boolean> emitter, String email) {
        Query fbRef = FirebaseUtil.getUsersRef().orderByChild(FirebaseUtil.EMAIL_KEY).equalTo(email);
        if (fbRef == null) {
            emitter.onNext(false);
            emitter.onComplete();
            return;
        }
        fbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference friendsRef = FirebaseUtil.getFriendsRef();
                if (friendsRef == null) {
                    emitter.onNext(false);
                    emitter.onComplete();
                    return;
                }

                ArrayList<User> users = receiveUsersFromFirebase(dataSnapshot);
                // Добавим в список друзей в FireBase
                for (User newFriend : users) {
                    Map<String, Object> updateValues = new HashMap<>();
                    updateValues.put("name", newFriend.getName());
                    updateValues.put("photoUrl", newFriend.getPhotoUrl());
                    updateValues.put("userEmail", newFriend.getUserEmail());
                    friendsRef.child(newFriend.getUid()).updateChildren(updateValues,
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                    if (firebaseError != null) {
                                        emitter.onError(new Throwable(firebaseError.getMessage()));
                                    }
                                }
                            });
                }

                emitter.onNext(users.size() > 0);
                emitter.onComplete();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(R.string.connection_failed + " " + databaseError.getCode());
                    }
                });
                emitter.onComplete();
            }
        });
    }

    private static ArrayList<User> receiveUsersFromFirebase(DataSnapshot dataSnapshot) {
        ArrayList<User> users = new ArrayList<>();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            User friend = childDataSnapshot.getValue(User.class);
            friend.setUid(childDataSnapshot.getKey());
            users.add(friend);
        }
        return users;
    }

    public static Observable<Boolean> endLoadingToAdapterObservable() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                ValueEventListener fbEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        emitter.onNext(true);
                        emitter.onComplete();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        emitter.onError(new Throwable(databaseError.getMessage()));
                    }
                };
                FirebaseUtil.getBaseRef().addListenerForSingleValueEvent(fbEventListener);
            }
        });
    }

    public static Observable<Boolean> sendShoppingListToFBObservable(final ShoppingList shoppingList,
                                                                      final Context context, final String userKey) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                // Отправим список покупок в FireBase
                Map<String, Object> updateValues = new HashMap<>();
                updateValues.put("content", shoppingList.convertShoppingListToString(context));
                User currentUser = FirebaseUtil.readUserFromPref(context);
                updateValues.put("author", currentUser);
                updateValues.put("name", shoppingList.getName());
                // Сформируем идентифиатор для списка - <id автора>_<название списка>
                String userUid = (currentUser == null) ? "" : (currentUser.getUid() == null) ? "" : currentUser.getUid();
                String listId = userUid + "_" + shoppingList.getNameForFirebase();
                FirebaseUtil.getUsersRef().child(userKey).child(FirebaseUtil.SHOPPING_LISTS_PATH)
                        .child(listId).updateChildren(updateValues,
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                if (firebaseError != null) {
                                    emitter.onError(new Throwable(context.getString(R.string.error_savin_user_data)));
                                }else{
                                    emitter.onNext(true);
                                    emitter.onComplete();
                                }
                            }
                        });
            }
        });
    }

    public static Observable<Boolean> addFriendObservable(final User user) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                addUserToTable(FirebaseUtil.getFriendsRef(), user, emitter);
            }
        });
    }

    public static Observable<Boolean> addUserToBlackListObservable(final User user) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                addUserToTable(FirebaseUtil.getBlackListRef(), user, emitter);
            }
        });
    }

    private static void addUserToTable(DatabaseReference tableRef, final User user, final ObservableEmitter<Boolean> emitter){
        if (user == null || tableRef == null) {
            emitter.onNext(false);
            emitter.onComplete();
            return;
        }

        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("name", user.getName());
        updateValues.put("photoUrl", user.getPhotoUrl());
        updateValues.put("userEmail", user.getUserEmail());
        tableRef.child(user.getUid()).updateChildren(updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        if (firebaseError != null) {
                            emitter.onError(new Throwable(firebaseError.getMessage()));
                        }else{
                            emitter.onNext(true);
                            emitter.onComplete();
                        }
                    }
                });
    }

    public static Observable<AuthResult> signInObservable(final FirebaseAuth auth, final AuthCredential credential) {
        return Observable.create(new ObservableOnSubscribe<AuthResult>() {
            @Override
            public void subscribe(final ObservableEmitter<AuthResult> emitter) throws Exception {
                auth.signInWithCredential(credential)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult result) {
                                emitter.onNext(result);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                emitter.onError(new Throwable(e.getMessage()));
                            }
                        });
            }
        });
    }
}

package com.app.promotionapp.retailer_repository;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.app.promotionapp.LoginActivity;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.User;
import com.app.promotionapp.retailer.MainActivity;
import com.app.promotionapp.user.UserDashboard;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RetailerRepository {
    ProgressDialog progressDialog;

    Activity context;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    MutableLiveData<List<User>> allUsers;
    MutableLiveData<List<Store>> allStores;
    List<Store> storeList;

    public RetailerRepository(Activity context) {
        this.context = context;

        storeList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(context);

        progressDialog.setTitle("Promotion App");
        progressDialog.setMessage("Loading, please wait. . .");

        allUsers = new MutableLiveData<>();
        allStores = new MutableLiveData<>();
    }

    public void registerRetailer(User retailer) {
        signupWithEmailAndPassword(retailer);
    }

    public void loginRetailer(String email, String password) {
        loginWithEmailAndPassword(email, password);
    }

    public MutableLiveData<List<User>> getAllRetailers() {
        return getAllUsers();
    }

    public void createPlace(Store store) {
        createStore(store);
    }

    public MutableLiveData<List<Store>> getAllStores(String uid) {
        return getAllRetailerStores(uid);
    }

    private void signupWithEmailAndPassword(User retailer) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(retailer.getEmail(), retailer.getPassword())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        registerUser(retailer);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in creating retailer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithEmailAndPassword(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(mAuth.getCurrentUser().getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        progressDialog.dismiss();
                                        User user = documentSnapshot.toObject(User.class);
                                        if(user.isRetailer()){
                                            context.startActivity(new Intent(context, MainActivity.class));
                                            return;
                                        }else{
                                            context.startActivity(new Intent(context, UserDashboard.class));
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in loging: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(User user) {
        progressDialog.show();
        firestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        context.startActivity(new Intent(context, LoginActivity.class));
                        Toast.makeText(context, "Retailer registered successfully !", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Registering Failed !", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void createStore(Store store) {
        progressDialog.show();
        firestore.collection("Stores")
                .document()
                .set(store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        createStoreWithRetailerAccount(store);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in creating store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStoreWithRetailerAccount(Store store) {
        firestore.collection("Retailers")
                .document(store.getUser_id())
                .collection("Stores")
                .document().set(store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Store Created Successfully !", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in creating store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MutableLiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> allUser = new MutableLiveData<>();
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> userList = new ArrayList<>();
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documentSnapshot : documents) {
                            userList.add(documentSnapshot.toObject(User.class));
                        }

                        allUser.postValue(userList);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Exception in getting all users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return allUser;
    }

    private MutableLiveData<List<Store>> getAllRetailerStores(String uid) {
        MutableLiveData<List<Store>> allStores = new MutableLiveData<>();
        firestore.collection("Retailers")
                .document(uid)
                .collection("Stores")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : allDocs) {
                            storeList.add(snapshot.toObject(Store.class));
                        }
                        allStores.setValue(storeList);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Exception in getting all Stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return allStores;
    }
}

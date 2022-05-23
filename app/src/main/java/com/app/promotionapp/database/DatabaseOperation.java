package com.app.promotionapp.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.promotionapp.LoginActivity;
import com.app.promotionapp.NotificationPack.Token;
import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.app.promotionapp.retailer.MainActivity;
import com.app.promotionapp.retailer.RetailerHomeFragment;
import com.app.promotionapp.user.UserDashboard;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class DatabaseOperation {
    private static DatabaseOperation databaseInstance = null;

    Activity context;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseDatabase firebaseDatabase;

    ProgressDialog progressDialog;

    private DatabaseOperation(Activity context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, please wait");
    }

    public static DatabaseOperation getDatabaseInstance(Activity context){
//        this.context = context;
        if(databaseInstance == null){
            databaseInstance = new DatabaseOperation(context);
        }
        return databaseInstance;
    }

    public void registerUser(User user) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        registerUserInDatabase(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in creating retailer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUserInDatabase(User user) {
        progressDialog.show();
        firestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        context.startActivity(new Intent(context, LoginActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Registering Failed !", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        loginWithRetailerOrUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in loging: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginWithRetailerOrUser() {
        firestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();
                        User user = documentSnapshot.toObject(User.class);
                        if (user.isRetailer()) {
                            context.startActivity(new Intent(context, MainActivity.class));
                            return;
                        } else {
                            context.startActivity(new Intent(context, UserDashboard.class));
                            updateToken();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private void updateToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Token token = new Token(s);
                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .set(token);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to get token !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void createStore(Store store){
        progressDialog.show();
        firestore.collection("Stores")
                .document()
                .set(store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        context.startActivity(new Intent(context, RetailerHomeFragment.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in creating store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addToSubscribeStores(SubscribedStore store){
        progressDialog.show();
        FirebaseFirestore.getInstance()
                .collection("Subscribed")
                .document()
                .set(store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "You will be given notifications regarding new products !", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Exception in Subscribing the store: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.app.promotionapp.user_repository;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    Activity context;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;

    public UserRepository(Activity context) {
        this.context = context;

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait...");
    }

    public void loginUser(String email, String password) {
        loginWithEmailAndPassword(email, password);
    }

    private void loginWithEmailAndPassword(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        context.startActivity(new Intent(context, UserRepository.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error in loging: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addStoreToFavourites(SubscribedStore store){
        setSelectedStoreSubscribed(store);
    }
    private void setSelectedStoreSubscribed(SubscribedStore store) {
        FirebaseFirestore.getInstance()
                .collection("Subscribed")
                .document()
                .set(store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        setStoreSubscribedForCurrentUser(store);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Exception in Subscribing the store: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void setStoreSubscribedForCurrentUser(SubscribedStore store) {
//        FirebaseFirestore.getInstance()
//                .collection("Users")
//                .document(mAuth.getCurrentUser().getUid())
//                .collection("Subscribed")
//                .document()
//                .set(store)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Toast.makeText(context, "You will start getting notification for the latest products !", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(context, "Exception in subscribing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}

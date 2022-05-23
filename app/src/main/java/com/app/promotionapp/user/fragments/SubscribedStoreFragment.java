package com.app.promotionapp.user.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.promotionapp.R;
import com.app.promotionapp.adapter.UserSubscribedAdapter;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubscribedStoreFragment extends Fragment {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    GridView gridView;
    TextView userSubscribedFrag_userName;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_subscribed_store, container, false);
        gridView = view.findViewById(R.id.userSubscribedGridView);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userSubscribedFrag_userName = view.findViewById(R.id.userSubscribedFrag_userName);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait");
        initData();
        return  view;
    }

    private void initData() {
        progressDialog.show();
        setCurrentUserInfo();
        String uid = mAuth.getCurrentUser().getUid();
        firestore.collection("Subscribed").whereEqualTo("subscribedUserID", uid)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressDialog.dismiss();
                List<SubscribedStore> subscribedStores = new ArrayList<>();
                if(queryDocumentSnapshots.getDocuments().size()>0){
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot documentSnapshot: documentSnapshots){
                        subscribedStores.add(documentSnapshot.toObject(SubscribedStore.class));
                    }
                }
                UserSubscribedAdapter adapter = new UserSubscribedAdapter(subscribedStores);
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error in getting your favourite stores: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCurrentUserInfo() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();
                        User user = documentSnapshot.toObject(User.class);
                        userSubscribedFrag_userName.setText("Helllo "+user.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Exception in getting current user: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.app.promotionapp.user.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.promotionapp.LoginActivity;
import com.app.promotionapp.R;
import com.app.promotionapp.adapter.SettingfragAdapter;
import com.app.promotionapp.adapter.UserSubscribedAdapter;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {

    TextInputLayout userName;
    TextInputLayout userEmail;
    RecyclerView subscribedStores;

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    ImageView logout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userName = view.findViewById(R.id.settingFragment_userName);
        userEmail = view.findViewById(R.id.settingFragment_userEmail);
        subscribedStores = view.findViewById(R.id.settingFragment_subscribedStores);
        logout = view.findViewById(R.id.userDashboard_logoutMenu);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                mAuth.signOut();
                Toast.makeText(getActivity(), "Signed Out !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                progressDialog.dismiss();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        subscribedStores.setLayoutManager(linearLayoutManager);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait");
        initData();
        return view;
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
                List<SubscribedStore> subscribed = new ArrayList<>();
                if(queryDocumentSnapshots.getDocuments().size()>0){
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot documentSnapshot: documentSnapshots){
                        subscribed.add(documentSnapshot.toObject(SubscribedStore.class));
                    }
                }
                SettingfragAdapter adapter = new SettingfragAdapter(subscribed);
                subscribedStores.setAdapter(adapter);
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
                        userName.getEditText().setText(user.getName());
                        userEmail.getEditText().setText(user.getEmail());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Exception in getting current user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
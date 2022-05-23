package com.app.promotionapp.retailer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.promotionapp.LoginActivity;
import com.app.promotionapp.R;
import com.app.promotionapp.adapter.RetailerSettingFragAdapter;
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

public class RetailerSettingFragment extends Fragment {

    TextInputLayout userName;
    TextInputLayout userEmail;
    RecyclerView retailerSubscribedStores;

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    ImageView logout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_retailer_setting, container, false);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        logout = view.findViewById(R.id.retailer_logoutMenu);

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
        userName = view.findViewById(R.id.retailerSettingFragment_userName);
        userEmail = view.findViewById(R.id.retailerSettingFragment_userEmail);
        retailerSubscribedStores = view.findViewById(R.id.retailerSettingFragment_subscribedStores);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        retailerSubscribedStores.setLayoutManager(linearLayoutManager);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait");
        initData();
        return view;
    }

    private void initData() {
        progressDialog.show();
        String uid = mAuth.getCurrentUser().getUid();
        setCurrentUserInfo();
        firestore
                .collection("Subscribed")
                .whereEqualTo("retailer_id",uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<SubscribedStore> subscribedStoreList = new ArrayList<>();
                        if (queryDocumentSnapshots.getDocuments().size() > 0) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                subscribedStoreList.add(documentSnapshot.toObject(SubscribedStore.class));
                            }
                        }

                        RetailerSettingFragAdapter adapter = new RetailerSettingFragAdapter(subscribedStoreList);
                        retailerSubscribedStores.setAdapter(adapter);
                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Exception in getting subscribed Stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

package com.app.promotionapp.retailer;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.promotionapp.R;
import com.app.promotionapp.adapter.RetailerAdapter;
import com.app.promotionapp.adapter.UserSubscribedAdapter;
import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RetailerHomeFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayout noStoreLayout;
    RelativeLayout storeLayout;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;
    TextView currentUserName;
    TextView createPlaceBtn;
    FloatingActionButton createPlaceFloatingActionBtn;

    DatabaseOperation databaseInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_retailer_home, container, false);
        mAuth =FirebaseAuth.getInstance();
        databaseInstance = DatabaseOperation.getDatabaseInstance(getActivity());
        initUI(view);
        initData();

        createPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MapsActivity.class));
            }
        });

        createPlaceFloatingActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MapsActivity.class));
            }
        });
        return view;
    }

    private void initUI(View view) {
        firestore = FirebaseFirestore.getInstance();
        currentUserName = view.findViewById(R.id.currentUserName);
        noStoreLayout = view.findViewById(R.id.noStoreLayout);
        storeLayout = view.findViewById(R.id.storeLayout);
        createPlaceFloatingActionBtn = view.findViewById(R.id.retailers_createPlaceBtn);
        createPlaceBtn = view.findViewById(R.id.retailerHomeFrag_onCreatePlace);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait. . .");
        progressDialog.show();
        recyclerView = view.findViewById(R.id.retailers_createdPlaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

    }

    private void initData() {
        setUserName();
        String uid = mAuth.getCurrentUser().getUid();
        firestore.collection("Stores")
                .whereEqualTo("user_id", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().size() == 0){
                            progressDialog.dismiss();
                            storeLayout.setVisibility(View.GONE);
                            noStoreLayout.setVisibility(View.VISIBLE);
                            return;
                        }
                        List<Store> storeList = new ArrayList<>();
                        List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot snapshot:allDocs){
                            storeList.add(snapshot.toObject(Store.class));
                        }

                        RetailerAdapter adapter = new RetailerAdapter(storeList);
                        recyclerView.setAdapter(adapter);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error in getting current user stores: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserName() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        currentUserName.setText("Welcome "+user.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Exception in getting current user: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
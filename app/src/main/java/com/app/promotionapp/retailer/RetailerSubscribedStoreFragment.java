package com.app.promotionapp.retailer;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.app.promotionapp.NotificationPack.NotificationHelper;
import com.app.promotionapp.NotificationPack.Token;
import com.app.promotionapp.R;
import com.app.promotionapp.adapter.RetailerSubscribedAdapter;
import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RetailerSubscribedStoreFragment extends Fragment {

    TextView userSubscribedFrag_userName;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    FirebaseFirestore firestore;
    GeoFire geoFire;
    GridView gridView;

    DatabaseOperation databaseInstance;
    
    double storeLatitude = 0.0;
    double storeLongitude = 0.0;
    String userID= "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribed_store, container, false);
        initUI(view);
        databaseInstance = DatabaseOperation.getDatabaseInstance(getActivity());
        initData();
        return view;
    }

    private void initUI(View view) {
        userSubscribedFrag_userName = view.findViewById(R.id.userSubscribedFrag_userName);
        gridView = view.findViewById(R.id.userSubscribedGridView);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Subscribed-User");
        geoFire = new GeoFire(userRef);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait");

    }

    private void initData() {
        progressDialog.show();
        setCurrentUserInfo();
        String uid = mAuth.getCurrentUser().getUid();

        firestore.collection("Subscribed")
                .whereEqualTo("retailer_id", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressDialog.dismiss();
                        List<SubscribedStore> subscribedStores = new ArrayList<>();
                        if (queryDocumentSnapshots.getDocuments().size() > 0) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                SubscribedStore store = documentSnapshot.toObject(SubscribedStore.class);
                                subscribedStores.add(store);
                            }
                        }
                        notifyUserAboutProduct(subscribedStores);
                        RetailerSubscribedAdapter adapter = new RetailerSubscribedAdapter(subscribedStores);
                        gridView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        notifyUserAboutProduct(subscribedStores);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Exception in getting subscribed Stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyUserAboutProduct(List<SubscribedStore> subscribedStores) {
        for(int i=0;i<subscribedStores.size();i++){
            SubscribedStore store = subscribedStores.get(i);
            userID = store.getSubscribedUserID();
            String [] storeCoordinates = subscribedStores.get(i).getLocationCoordinates().split(",");
            storeLatitude = Double.parseDouble(storeCoordinates[0]);
            storeLongitude = Double.parseDouble(storeCoordinates[1]);

            geoFire.getLocation(userID, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if(location == null){
                        return;
                    }
                    double latitude = location.latitude;
                    double longitude = location.longitude;
                    double distanceFromStore = getDistanceBetweenLocations(latitude, longitude,storeLatitude,storeLongitude);

                    if(distanceFromStore < 7){
                        notifyUserWithNotification(store);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void notifyUserWithNotification(SubscribedStore store) {
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot: documentSnapshots){
                    Token token = documentSnapshot.toObject(Token.class);
                    String message = getString(R.string.notificationMessage);
                    sendNotifications(token.getToken(),store.getStore_title(), message);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error in getting token: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendNotifications(String token, String title, String message) {
        NotificationHelper.displayNotification(getActivity(), title, message);
//        SendNotificationModel sendNotificationModel = new SendNotificationModel(title, message);
//        RequestNotificaton requestNotificaton = new RequestNotificaton();
//        requestNotificaton.setSendNotificationModel(sendNotificationModel);
//        requestNotificaton.setToken(token);
//
//        apiService =  Client.getClient().create(APIService.class);
//        retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendChatNotification(requestNotificaton);
//
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//                Log.d("kkkk","done");
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
//
//            }
//        });
    }
    private double getDistanceBetweenLocations(double lat1, double lon1, double lat2, double lon2) {
        DecimalFormat df = new DecimalFormat("0.00");
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        ;
        return (Double.parseDouble(df.format(dist)));
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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
                        userSubscribedFrag_userName.setText("Hello " + user.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Exception in getting current user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
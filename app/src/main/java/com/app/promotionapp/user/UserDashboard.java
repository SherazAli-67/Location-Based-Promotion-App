
package com.app.promotionapp.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.promotionapp.NotificationPack.NotificationHelper;
import com.app.promotionapp.NotificationPack.Token;
import com.app.promotionapp.R;
import com.app.promotionapp.adapter.RetailerSubscribedAdapter;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.user.fragments.HomeFragment;
import com.app.promotionapp.user.fragments.SettingFragment;
import com.app.promotionapp.user.fragments.SubscribedStoreFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class UserDashboard extends AppCompatActivity {

    BottomNavigationView bottomNav;

    double storeLatitude = 0.0;
    double storeLongitude = 0.0;
    GeoFire geoFire;
    DatabaseReference userRef;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        bottomNav = (BottomNavigationView) findViewById(R.id.userDashboard_bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(mBottomListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,new HomeFragment()).commit();

        userRef = FirebaseDatabase.getInstance().getReference("Subscribed-User");
        geoFire = new GeoFire(userRef);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        checkForNotification();
    }

    private void checkForNotification() {
        String userID = mAuth.getCurrentUser().getUid();

        firestore.collection("Subscribed")
                .whereEqualTo("subscribedUserID", userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        progressDialog.dismiss();
                        List<SubscribedStore> subscribedStores = new ArrayList<>();
                        if (queryDocumentSnapshots.getDocuments().size() > 0) {
                            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                SubscribedStore store = documentSnapshot.toObject(SubscribedStore.class);
                                subscribedStores.add(store);
                            }
                        }

                        notifyUserAboutProduct(subscribedStores);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Exception in getting subscribed Stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mBottomListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment;
            switch (item.getItemId()){
                case R.id.home_item:
                    selectedFragment = new HomeFragment();
                    break;

                case R.id.subscribed_item:
                    selectedFragment = new SubscribedStoreFragment();
                    break;
                case R.id.setting_item:
                    selectedFragment = new SettingFragment();
                    break;

                default:
                    selectedFragment = new HomeFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, selectedFragment).commit();
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }

    private void notifyUserAboutProduct(List<SubscribedStore> subscribedStores) {
        for(int i=0;i<subscribedStores.size();i++){
            SubscribedStore store = subscribedStores.get(i);
            String userID = store.getSubscribedUserID();
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
//                    Toast.makeText(getApplicationContext(), "Distance: "+distanceFromStore, Toast.LENGTH_SHORT).show();
                    if(distanceFromStore < 5){
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
                Toast.makeText(getApplicationContext(), "Error in getting token: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void sendNotifications(String token, String title, String message) {
        NotificationHelper.displayNotification(UserDashboard.this, title, message);
    }

}
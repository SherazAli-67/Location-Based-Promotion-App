package com.app.promotionapp.user.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.app.promotionapp.NotificationPack.Token;
import com.app.promotionapp.R;
import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, LocationListener {
    private static final int LOCATION_REQUESTCODE = 23232;
    BottomSheetDialog confirmSubscription;
    TextInputLayout shopTitle;
    TextInputLayout subscribedUser;
    TextView locationCoordinates;
    TextView confirmSubscriptionBtn;
    TextView currentUserName;

    public static String USERNAME = "";
    public static String SHOPTITLE = "";
    public static String LOCATION_COORDINATES = "";

    MapView mMapView;
    GoogleMap googleMap;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback mLocationCallback;
    Location mLastLocation;

    GeoFire geoFire;
    DatabaseReference userRef;

    DatabaseOperation databaseInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        initUI(view);
        databaseInstance = DatabaseOperation.getDatabaseInstance(getActivity());

//        updateToken();
        confirmSubscriptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = mAuth.getCurrentUser().getUid();
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                geoFire.setLocation(uid, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Storing user Location
                    }
                });

                handleConfirmSubscription();
            }
        });
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null)
                    return;
                mLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error in getting Current Location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                mLastLocation = location;
            }
        };
        initData();
        return view;
    }

    private void handleConfirmSubscription() {
        firestore.collection("Stores")
                .whereEqualTo("store_title", SHOPTITLE)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String retailerID = "";
                        String storeID = "";
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Store store = documentSnapshot.toObject(Store.class);
                            retailerID = store.getUser_id();
                            storeID = store.getStore_id();
                        }

                        String userLocationCoordinates = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                        SubscribedStore subscribedStore = new SubscribedStore(storeID, SHOPTITLE, retailerID,
                                LOCATION_COORDINATES, mAuth.getCurrentUser().getUid(), userLocationCoordinates);
                        databaseInstance.addToSubscribeStores(subscribedStore);
                        confirmSubscription.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error in getting Store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI(View view) {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userRef = FirebaseDatabase.getInstance().getReference("Subscribed-User");
        geoFire = new GeoFire(userRef);

        currentUserName = view.findViewById(R.id.homeFrag_currentUserName);
        confirmSubscription = new BottomSheetDialog(getActivity());
        confirmSubscription.setContentView(R.layout.confirm_subscription_bottomsheet);
        shopTitle = confirmSubscription.findViewById(R.id.confirmSubscription_shopName);
        subscribedUser = confirmSubscription.findViewById(R.id.confirmSubscription_subscribedUser);
        locationCoordinates = confirmSubscription.findViewById(R.id.confirmSubscription_locationCoordinates);
        confirmSubscriptionBtn = confirmSubscription.findViewById(R.id.confirmSubscription_confirmSubscription);
        fusedLocationProviderClient = new FusedLocationProviderClient(getActivity());


    }


    private void initData() {
        setUserName();
        String uid = mAuth.getCurrentUser().getUid();
        firestore.collection("Subscribed")
                .whereEqualTo("subscribedUserID", uid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<SubscribedStore> subscribedStores = new ArrayList<>();
                if (queryDocumentSnapshots.getDocuments().size() > 0) {
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                        subscribedStores.add(documentSnapshot.toObject(SubscribedStore.class));
                    }
                }

                firestore.collection("Stores").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Store> allStores = new ArrayList<>();
                        List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : allDocs) {
                            allStores.add(snapshot.toObject(Store.class));
                        }

                        for (int i = 0; i < allStores.size(); i++) {
                            for (int j = 0; j < subscribedStores.size(); j++) {
                                if (allStores.get(i).getStore_id().equals(subscribedStores.get(j).getStore_id())
                                        && allStores.get(i).getUser_id().equals(subscribedStores.get(j).getRetailer_id())) {
                                    allStores.remove(allStores.get(i));
                                }
                            }
                        }

                        for (int i = 0; i < allStores.size(); i++) {
                            Store store = allStores.get(i);
                            String[] coordinates = store.getLocationCoordiantes().split(",");
                            double latitude = Double.parseDouble(coordinates[0]);
                            double longitude = Double.parseDouble(coordinates[1]);
                            LatLng latLng = new LatLng(latitude, longitude);

                            googleMap.addMarker(new MarkerOptions().
                                    position(latLng).
                                    title(store.getStore_title()).
                                    icon(BitmapDescriptorFactory.fromResource(R.drawable.shop)));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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
                        USERNAME = user.getName();
                        currentUserName.setText("Welcome " + USERNAME);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error in getting current User", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUESTCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Please Enable Location", Toast.LENGTH_SHORT).show();
                    return;
                }
//                fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
            } else {
                Toast.makeText(getContext(), "Permission Denied !", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Error in getting updates!", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
//        geoFire.removeLocation(mAuth.getCurrentUser().getUid());
        super.onStop();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap mMap) {
        googleMap = mMap;
        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        getActivity(), R.raw.custom_style));

        if (!success) {
            Toast.makeText(getContext(), "Parsing Style get failed!", Toast.LENGTH_SHORT).show();
        }
        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (mLastLocation == null) {
            Toast.makeText(getContext(), "Please Enable Location First !", Toast.LENGTH_SHORT).show();
            return false;
        }
        SHOPTITLE = marker.getTitle();
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        LOCATION_COORDINATES = latitude + "," + longitude;

        shopTitle.getEditText().setText(SHOPTITLE);
        subscribedUser.getEditText().setText(USERNAME);
        locationCoordinates.setText(LOCATION_COORDINATES);
        confirmSubscription.show();

        return true;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
    }
}
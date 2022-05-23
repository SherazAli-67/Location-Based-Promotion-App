package com.app.promotionapp.retailer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RetailerMapFragment extends Fragment implements OnMapReadyCallback {
    MapView mMapView;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_retailer_map, container, false);

        mMapView = view.findViewById(R.id.retailerMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initMapData();
        return view;
    }

    private void initMapData() {
        firestore.collection("Retailers")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Stores")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Store> userStores = new ArrayList<>();

                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                            Store store = documentSnapshot.toObject(Store.class);
                            String[] coordinates = store.getLocationCoordiantes().split(",");
                            double latitude = Double.parseDouble(coordinates[0]);
                            double longitude = Double.parseDouble(coordinates[1]);

                            mMap.addMarker(new MarkerOptions().title(store.getStore_title())
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop)));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error in getting Stores: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        firestore.collection("Subscribed")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot documentSnapshot: documentSnapshots){
                            SubscribedStore stores = documentSnapshot.toObject(SubscribedStore.class);
                            if(stores.getRetailer_id().equals(mAuth.getCurrentUser().getUid())){
                                String[] coordinates = stores.getUserCoordinates().split(",");
                                double latitude = Double.parseDouble(coordinates[0]);
                                double longitude = Double.parseDouble(coordinates[1]);

                                mMap.addMarker(new MarkerOptions());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error in getting subscribed Users: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

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
    }
}
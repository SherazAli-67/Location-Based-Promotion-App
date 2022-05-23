package com.app.promotionapp.retailer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.app.promotionapp.R;
import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.model.Store;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_CODE = 101;
    private GoogleMap mMap;

    EditText locationName;
    ImageView locationIcon;

    BottomSheetDialog bottomSheetDialog;
    TextView locationTitle;
    TextView subLocation;
    TextView locationCoordinates;
    TextView confirmLocation;
    TextInputLayout shopTitle;
    AutoCompleteTextView categories;

    String searhedLocationTitle = "";
    String searhedLocationSubtitle = "";
    String searhedLocationCoordinates = "";
    String selectedCategory = "";
    FirebaseAuth mAuth;

    FusedLocationProviderClient fusedLocationProviderClient;
    Location mLastLocation;
    LocationCallback mLocationCallback;

    DatabaseOperation databaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initUI();
        mAuth = FirebaseAuth.getInstance();
        databaseInstance = DatabaseOperation.getDatabaseInstance(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLocationIconClick();
            }
        });

        categories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Location !", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(getApplicationContext(), "Enable Location First !", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error in getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    Toast.makeText(getApplicationContext(), "Enable Location !", Toast.LENGTH_SHORT).show();
                } else {
                    mLastLocation = locationResult.getLastLocation();
                }
            }
        };
    }

    private void handleLocationIconClick() {
        searhedLocationTitle = locationName.getText().toString();

        if (searhedLocationTitle.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Location First !", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            Address address = geocoder.getFromLocationName(searhedLocationTitle, 1).get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();
            searhedLocationSubtitle = address.getLocality() + ", " + address.getAdminArea() + ", "
                    + address.getCountryName();
            searhedLocationCoordinates = latitude + "," + longitude;
            locationTitle.setText(searhedLocationTitle);
            subLocation.setText(searhedLocationSubtitle);
            locationCoordinates.setText(searhedLocationCoordinates);
            bottomSheetDialog.show();

            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title(searhedLocationTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initUI() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.confirm_location_bottomsheet);
        locationTitle = bottomSheetDialog.findViewById(R.id.bottomSheet_location);
        subLocation = bottomSheetDialog.findViewById(R.id.bottomSheet_subLocation);
        locationCoordinates = bottomSheetDialog.findViewById(R.id.bottomSheet_locationCoordinates);
        confirmLocation = bottomSheetDialog.findViewById(R.id.bottomSheet_confirmLocation);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationIcon = findViewById(R.id.searchImg);
        locationName = findViewById(R.id.searchLocationName);
        shopTitle = bottomSheetDialog.findViewById(R.id.bottomSheet_shopName);
//        shopOwner = bottomSheetDialog.findViewById(R.id.bottomSheet_shopOwner);
        categories = bottomSheetDialog.findViewById(R.id.bottomSheet_category);

        List<String> categoriesList = getCategories();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoriesList);
        categories.setAdapter(adapter);
    }

    public void onConfirmLocationClick(View view) {
        if (DataIsValid()) {
            bottomSheetDialog.dismiss();
            String shop_title = shopTitle.getEditText().getText().toString();
            String retailerID = mAuth.getCurrentUser().getUid();

            Store store = new Store(getAlphaNumericString(9), shop_title, searhedLocationTitle,
                    searhedLocationSubtitle, searhedLocationCoordinates, retailerID);
            databaseInstance.createStore(store);
            locationName.setText("");
            shopTitle.getEditText().setText("");
            bottomSheetDialog.dismiss();

        } else {
            bottomSheetDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Empty Fields are not acceptable !", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().isZoomControlsEnabled();
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.custom_style));
            mMap.setOnMarkerClickListener(this);
            if (!success) {
                Toast.makeText(getApplicationContext(), "Parsing Style get failed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestForlocationPermission();
        }

    }

    private void requestForlocationPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Granted !", Toast.LENGTH_SHORT).show();
        } else {
            requestForlocationPermission();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        locationTitle.setText(searhedLocationTitle);
        subLocation.setText(searhedLocationSubtitle);
        locationCoordinates.setText(searhedLocationCoordinates);
        bottomSheetDialog.show();
        return true;
    }

    private boolean DataIsValid() {
        String shop_title = shopTitle.getEditText().getText().toString();

        if (shop_title.isEmpty()) {
            return false;
        } else
            return true;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();

        categories.add("Clothes");
        categories.add("Shoes");
        categories.add("Books");
        categories.add("Electronics");

        return categories;
    }

    static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    @Override
    protected void onStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
        super.onStart();
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }
}
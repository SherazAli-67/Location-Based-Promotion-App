package com.app.promotionapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private static final int LOCATION_REQUESTCODE = 111;
    private static final int REQUEST_CODE_READ_CONTACTS = 222;
    TextInputLayout name, email, password;
    TextView login, signup;
    AutoCompleteTextView loginAs;
    List<String> userOrRetailer;
    public String RETAILER_TITLE = "Retailer";
    public String USER_TITLE = "User";
    boolean isRetailer = false;
    FirebaseAuth mAuth;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback mLocationCallback;
    Location mLastLocation;

    DatabaseOperation databaseInstance;
    AudioManager am;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initUI();
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        databaseInstance = DatabaseOperation.getDatabaseInstance(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestForLocationPermission();
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
            }
        };

        if (mAuth.getCurrentUser() != null) {
            databaseInstance.loginWithRetailerOrUser();
        }
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = name.getEditText().getText().toString();
                String userEmail = email.getEditText().getText().toString();
                String userPassword = password.getEditText().getText().toString();
                User currentUser = new User(userName, userEmail, userPassword, isRetailer);
                databaseInstance.registerUser(currentUser);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        loginAs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals(RETAILER_TITLE)) {
                    isRetailer = true;
                } else if (parent.getItemAtPosition(position).toString().equals(USER_TITLE)) {
                    isRetailer = false;
                }
            }
        });
        requestMutePhonePermsAndMutePhone();
    }

    private void initUI() {
        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.signupUserName);
        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        loginAs = findViewById(R.id.signup_loginAs);

        signup = findViewById(R.id.signup_createAccountBtn);
        login = findViewById(R.id.signup_LoginBtn);

        userOrRetailer = new ArrayList<>();
        userOrRetailer = getUserOrRetailer();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, userOrRetailer);
        loginAs.setAdapter(adapter);
    }

    private List<String> getUserOrRetailer() {
        List<String> list = new ArrayList<>();
        list.add("User");
        list.add("Retailer");

        return list;
    }

    private void requestForLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUESTCODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUESTCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Please Enable Location", Toast.LENGTH_SHORT).show();
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

//    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_READ_CONTACTS){
            if(resultCode == RESULT_OK){
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                this.requestDoNotDisturbPermission();
            }
        }
    }

    private void requestMutePhonePermsAndMutePhone() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if( Build.VERSION.SDK_INT >= 23 ) {
                this.requestDoNotDisturbPermission();
            }
        } catch ( SecurityException e ) {
            Log.d(TAG, "Security Exception: "+e.getMessage());
        }
    }

    private void requestDoNotDisturbPermission() {
        //TO SUPPRESS API ERROR MESSAGES IN THIS FUNCTION, since Ive no time to figrure our Android SDK suppress stuff
        if( Build.VERSION.SDK_INT < 23 ) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notificationManager.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else{
            // Ask the user to grant access
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult( intent, REQUEST_CODE_READ_CONTACTS );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isRingerSilent()){
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Please Enable Location", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
    }
    public boolean isRingerSilent()
    {
        AudioManager audioManager =
                (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
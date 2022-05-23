package com.app.promotionapp.retailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.app.promotionapp.R;
import com.app.promotionapp.user.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNav = findViewById(R.id.retailer_bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(mBottomSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.retailer_frame_container,new RetailerHomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mBottomSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment;
            switch (item.getItemId()){
                case R.id.retailer_home_item:
                    selectedFragment = new RetailerHomeFragment();
                    break;

                case R.id.retailer_subscribed_item:
                    selectedFragment = new RetailerSubscribedStoreFragment();
                    break;

                case R.id.retailer_setting_item:
                    selectedFragment = new RetailerSettingFragment();
                    break;

                default:
                    selectedFragment = new HomeFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.retailer_frame_container, selectedFragment).commit();
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
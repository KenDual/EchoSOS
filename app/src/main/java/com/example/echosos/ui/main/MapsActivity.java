package com.example.echosos.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echosos.R;
import com.example.echosos.utils.Permissions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("app_opened", null);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        // (Optional) bật MyLocation khi đã có quyền
        if (Permissions.hasAll(this, Permissions.LOCATION)) {
            try
            {
                gMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {}
        }
        LatLng start = new LatLng(10.762622, 106.660172); // HCMC demo
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 14f));
    }
}
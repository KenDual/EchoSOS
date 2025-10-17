package com.example.echosos.ui.main;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.utils.Permissions;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_map, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        SupportMapFragment map = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (map != null) map.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        if (Permissions.hasAll(requireContext(), Permissions.LOCATION)) {
            try {
                gMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }
        LatLng hcm = new LatLng(10.762622, 106.660172);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 14f));
    }
}
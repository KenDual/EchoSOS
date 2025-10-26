package com.example.echosos.ui.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.services.location.LocationService;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQ_LOC = 1010;

    private GoogleMap map;
    private TextView tvAccuracy;
    private Marker meMarker;
    private Circle accCircle;
    private boolean firstFix = true;
    private FusedLocationProviderClient fused;
    private View btnToggleLive;
    private View loading; // overlay từ view_loading.xml

    private final BroadcastReceiver locReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ctx, Intent i) {
            if (map == null || i == null) return;
            double lat = i.getDoubleExtra(LocationService.EXTRA_LAT, 0d);
            double lng = i.getDoubleExtra(LocationService.EXTRA_LNG, 0d);
            float acc  = i.getFloatExtra(LocationService.EXTRA_ACC, 0f);
            LatLng p = new LatLng(lat, lng);

            if (meMarker == null) {
                meMarker = map.addMarker(new MarkerOptions().position(p).title(getString(R.string.you)));
            } else {
                meMarker.setPosition(p);
            }

            if (accCircle == null) {
                accCircle = map.addCircle(new CircleOptions()
                        .center(p).radius(Math.max(0, acc))
                        .strokeWidth(2f).strokeColor(0x55007AFF).fillColor(0x22007AFF));
            } else {
                accCircle.setCenter(p);
                accCircle.setRadius(Math.max(0, acc));
            }

            if (tvAccuracy != null) {
                tvAccuracy.setText(String.format("±%.0f m", acc));
                tvAccuracy.setVisibility(View.VISIBLE);
            }

            if (firstFix) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 16f));
                firstFix = false;
                setLoading(false); // tắt loading khi có fix đầu
            }
        }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_map, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        tvAccuracy = v.findViewById(R.id.tvAccuracy);
        loading = v.findViewById(R.id.loadingOverlay);
        setLoading(true);

        fused = LocationServices.getFusedLocationProviderClient(requireContext());

        // START/STOP live
        btnToggleLive = v.findViewById(R.id.btnToggleLive);
        if (btnToggleLive != null) {
            applySafeModeState();
            btnToggleLive.setOnClickListener(x -> {
                if (Prefs.isSafeMode(requireContext())) {
                    android.widget.Toast.makeText(requireContext(),
                            getString(R.string.safe_mode_on), android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Permissions.hasAll(requireContext(), Permissions.LOCATION)) {
                    requestPermissions(Permissions.LOCATION, REQ_LOC);
                    return;
                }
                ContextCompat.startForegroundService(requireContext(), new Intent(requireContext(), LocationService.class));
                if (btnToggleLive instanceof android.widget.Button) {
                    ((android.widget.Button) btnToggleLive).setText(R.string.stop_live);
                }
            });
        }

        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFrag != null) mapFrag.getMapAsync(this);
    }

    @Override public void onResume() {
        super.onResume();
        applySafeModeState();
    }

    private void applySafeModeState() {
        if (btnToggleLive == null) return;
        boolean safe = Prefs.isSafeMode(requireContext());
        btnToggleLive.setEnabled(!safe);
        btnToggleLive.setAlpha(safe ? 0.5f : 1f);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(LocationService.ACTION_LOC);
        ContextCompat.registerReceiver(requireContext(), locReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onStop() {
        try { requireContext().unregisterReceiver(locReceiver); } catch (Exception ignored) {}
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        map = null; meMarker = null; accCircle = null; tvAccuracy = null; btnToggleLive = null; loading = null;
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == REQ_LOC && Permissions.hasAll(requireContext(), Permissions.LOCATION)) {
            ContextCompat.startForegroundService(requireContext(), new Intent(requireContext(), LocationService.class));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        map = gMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if (Permissions.hasAll(requireContext(), Permissions.LOCATION)) {
            try { map.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
            fetchLastKnownLocation();
        } else {
            LatLng hcm = new LatLng(10.762622, 106.660172);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 14f));
            setLoading(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocation() {
        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null && firstFix && map != null) {
                LatLng p = new LatLng(loc.getLatitude(), loc.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 16f));
                firstFix = false;
                setLoading(false);
            }
        }).addOnFailureListener(e -> setLoading(false));
    }

    private void setLoading(boolean show) {
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

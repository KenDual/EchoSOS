package com.example.echosos.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public final class LocationFetcher {
    private LocationFetcher(){}

    public static final class Fix {
        public final double lat, lng; public final float acc; public final String address;
        public Fix(double la, double ln, float ac, String ad){ lat=la; lng=ln; acc=ac; address=ad; }
    }

    public interface Callback { void onResult(Fix fix); }

    @SuppressLint("MissingPermission")
    public static void getCurrentFix(Context ctx, Callback cb){
        FusedLocationProviderClient c = LocationServices.getFusedLocationProviderClient(ctx);
        c.getLastLocation().addOnSuccessListener(loc -> {
            if (loc == null) { cb.onResult(null); return; }
            cb.onResult(new Fix(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), reverseGeocode(ctx, loc)));
        }).addOnFailureListener(e -> cb.onResult(null));
    }

    private static String reverseGeocode(Context ctx, Location loc){
        try {
            Geocoder g = new Geocoder(ctx, Locale.getDefault());
            List<Address> list = g.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                String line = a.getMaxAddressLineIndex() >= 0 ? a.getAddressLine(0) : null;
                return line != null ? line : a.getLocality();
            }
        } catch (Exception ignored) {}
        return "N/A";
    }
}

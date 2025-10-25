package com.example.echosos.services.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.echosos.R;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    public static final String CHANNEL_ID = "safety_location";
    private static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_LOC = "ECHOSOS.LOC_UPD";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";
    public static final String EXTRA_ACC = "acc";

    private FusedLocationProviderClient fused;
    private LocationCallback callback;

    @Override
    public void onCreate() {
        super.onCreate();
        fused = LocationServices.getFusedLocationProviderClient(this);
        createChannel();

        // Gửi broadcast mỗi lần có vị trí mới
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) return;
                double lat = result.getLastLocation().getLatitude();
                double lng = result.getLastLocation().getLongitude();
                float acc  = result.getLastLocation().getAccuracy();

                Intent i = new Intent(ACTION_LOC)
                        .putExtra(EXTRA_LAT, lat)
                        .putExtra(EXTRA_LNG, lng)
                        .putExtra(EXTRA_ACC, acc);
                sendBroadcast(i);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAsForeground();
        startLocationUpdates();
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!Permissions.hasAll(this, Permissions.LOCATION)) return;

        long interval = Prefs.getLocIntervalMs(this);   // mặc định bạn set 10000ms trong Prefs
        if (interval < 3000L) interval = 3000L;         // guard tối thiểu 3s

        LocationRequest req = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setMinUpdateIntervalMillis(Math.max(2000L, interval / 2))
                .build();

        fused.requestLocationUpdates(req, callback, getMainLooper());
    }

    private void startAsForeground() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Đang chia sẻ vị trí")
                .setContentText("EchoSOS đang cập nhật vị trí của bạn.")
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID, "Live Location", NotificationManager.IMPORTANCE_LOW));
            }
        }
    }

    @Override
    public void onDestroy() {
        if (fused != null && callback != null) {
            fused.removeLocationUpdates(callback);
        }
        super.onDestroy();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

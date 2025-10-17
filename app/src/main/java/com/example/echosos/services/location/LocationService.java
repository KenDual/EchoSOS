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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    public static final String CHANNEL_ID = "safety_location";
    private static final int NOTIFICATION_ID = 1001; // *** Đưa ID ra làm hằng số ***

    private FusedLocationProviderClient fused;
    private LocationCallback callback;

    // --- BẮT ĐẦU CÁC THAY ĐỔI QUAN TRỌNG ---

    @Override
    public void onCreate() {
        super.onCreate();
        // Chỉ khởi tạo các đối tượng cần thiết một lần
        fused = LocationServices.getFusedLocationProviderClient(this);
        createChannel();

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) return;
                // TODO: gửi lên Firestore/Room/broadcast UI (phase sau)
                // Location location = result.getLastLocation();
                // Log.d("LocationService", "New Location: " + location.getLatitude() + ", " + location.getLongitude());
            }
        };
    }

    /**
     * Phương thức này là điểm bắt đầu thực sự cho các tác vụ của Service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Bắt đầu foreground service một cách chính xác
        startAsForeground();
        // Bắt đầu yêu cầu cập nhật vị trí
        startLocationUpdates();

        // START_STICKY sẽ khởi động lại service nếu nó bị hệ thống tiêu diệt
        return START_STICKY;
    }

    @SuppressLint("MissingPermission") // *** Thêm chú thích để bỏ qua cảnh báo của Lint ***
    private void startLocationUpdates() {
        // Kiểm tra quyền vẫn rất quan trọng, đảm bảo nó được gọi từ nơi có quyền
        if (Permissions.hasAll(this, Permissions.LOCATION)) {
            LocationRequest req = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
                    .setMinUpdateIntervalMillis(5_000L)
                    .build(); // setWaitForAccurateLocation(true) không còn cần thiết với Builder pattern mới

            fused.requestLocationUpdates(req, callback, getMainLooper());
        }
    }

    private void startAsForeground() {
        // *** Sử dụng NotificationCompat.Builder để tương thích tốt hơn ***
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Đảm bảo icon này tồn tại
                .setContentTitle("Đang chia sẻ vị trí")
                .setContentText("EchSOS đang cập nhật vị trí của bạn để đảm bảo an toàn.")
                .setOngoing(true)
                .build();

        // Logic khởi chạy foreground service được đơn giản hóa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    // --- KẾT THÚC CÁC THAY ĐỔI QUAN TRỌNG ---

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
        super.onDestroy();
        if (fused != null && callback != null) {
            fused.removeLocationUpdates(callback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

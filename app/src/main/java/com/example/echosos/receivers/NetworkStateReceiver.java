package com.example.echosos.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.echosos.services.recording.UploadRetryService;
import com.example.echosos.utils.NetworkUtils;

public class NetworkStateReceiver extends BroadcastReceiver {

    public static final String ACTION_NETWORK_ONLINE  = "com.example.echosos.NETWORK_ONLINE";
    public static final String ACTION_NETWORK_OFFLINE = "com.example.echosos.NETWORK_OFFLINE";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean online = NetworkUtils.isOnline(context);

        // Phát broadcast nội bộ cho UI (MainActivity/Fragment có thể lắng nghe)
        Intent ui = new Intent(online ? ACTION_NETWORK_ONLINE : ACTION_NETWORK_OFFLINE);
        context.sendBroadcast(ui);

        // Khi có mạng lại → kích hoạt retry upload (dịch vụ ngắn, tự dừng)
        if (online) {
            Intent svc = new Intent(context, UploadRetryService.class);
            svc.putExtra("reason", "network_restored");
            context.startService(svc);
        }
    }
}

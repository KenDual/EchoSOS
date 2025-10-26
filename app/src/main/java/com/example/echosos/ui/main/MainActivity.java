package com.example.echosos.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.services.recording.UploadRetryService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private AlertDialog noInternetDialog;

    private final BroadcastReceiver netUi = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            String a = i.getAction();
            if ("com.example.echosos.NETWORK_OFFLINE".equals(a)) {
                showNoInternetDialog();
            } else if ("com.example.echosos.NETWORK_ONLINE".equals(a)) {
                dismissNoInternetDialog();
                // Online trở lại -> kick retry upload
                startService(new Intent(MainActivity.this, UploadRetryService.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView nav = findViewById(R.id.bottom_nav);

        nav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                f = new MapFragment();
            } else if (id == R.id.nav_contacts) {
                f = new ContactsFragment();
            } else if (id == R.id.nav_call) {
                f = new CallFragment();
            } else {
                f = new HomeFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter();
        f.addAction("com.example.echosos.NETWORK_ONLINE");
        f.addAction("com.example.echosos.NETWORK_OFFLINE");
        // FIX: dùng ContextCompat.registerReceiver với flag NOT_EXPORTED
        ContextCompat.registerReceiver(
                this,
                netUi,
                f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onStop() {
        try { unregisterReceiver(netUi); } catch (Exception ignored) {}
        dismissNoInternetDialog();
        super.onStop();
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;
        noInternetDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.no_internet)
                .setMessage(getString(R.string.no_internet))
                .setPositiveButton(R.string.open_wifi_settings,
                        (d, w) -> startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
        noInternetDialog.show();
    }

    private void dismissNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
    }
}
